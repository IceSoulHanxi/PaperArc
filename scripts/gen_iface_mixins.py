#!/usr/bin/env python3
"""从 paper-api jar 机械生成接口增补 mixin（A 类缺口：630 方法/106 接口）。
规则：全部声明为抽象方法（宿主实现已由 api 批次 @Unique 覆盖）。
签名用 javap 的擦除形态——描述符一致即满足调用点解析；泛型 Signature 不影响分派。
"""
import json, subprocess, re, os, sys

PAPER_JAR = '/home/hanxi/.gradle/caches/modules-2/files-2.1/io.papermc.paper/paper-api/1.21.1-R0.1-SNAPSHOT/6f1e11de8e85bb7d49bbf9f23f456ed547d8e28c/paper-api-1.21.1-R0.1-SNAPSHOT.jar'
OUT = 'common/src/main/java/dev/paperarc/mixin/common/apiiface'
PKG = 'dev.paperarc.mixin.common.apiiface'
data = json.load(open('docs/data/missing-api.json'))
ifaces = json.load(open('docs/data/interface-augment.json'))
want = {}  # ifqn -> set((name, desc_prefix))
for ifqn, tgt, ms in data:
    want[ifqn] = {(m[0], m[1]) for m in ms}

PRIM = {'B':'byte','C':'char','D':'double','F':'float','I':'int','J':'long','S':'short','Z':'boolean','V':'void'}
def typ(md, i):
    d=0
    while md[i]=='[': d+=1; i+=1
    if md[i]=='L':
        j=md.index(';',i); t=md[i+1:j].replace('/','.').replace('$','.'); i=j+1
    else:
        t=PRIM.get(md[i],md[i]); i+=1
    return t + '[]'*d, i

def sig_of(mn, md):
    i=1; ps=[]
    while md[i] != ')':
        t,i = typ(md,i); ps.append(t+' p'+str(len(ps)))
    r,_ = typ(md, i+1)
    return f'{r} {mn}({", ".join(ps)})'

os.makedirs(OUT, exist_ok=True)
gen = 0
for ifqn in sorted(ifaces):
    simple = ifqn.rsplit('.',1)[1]
    txt = subprocess.run(['javap','-cp',PAPER_JAR,ifqn],capture_output=True,text=True).stdout
    decls = []
    seen = set()
    for line in txt.splitlines():
        m = re.match(r'\s*(?:public |protected )?(?:static )?(?:default |abstract )?(?:<[^>]+>\s+)?([\w.$\[\]<>?, ]+) (\w+)\((.*)\);', line)
        if not m: continue
        mn = m.group(2)
        if mn == simple: continue  # 构造器不适用接口
        # 用描述符前缀精确匹配缺失清单，避免把已有方法也搬进去
        params = m.group(3)
        nparams = 0 if not params.strip() else len(params.split(','))
        key = None
        for (name, pre) in want.get(ifqn, ()):
            if name != mn: continue
            # 描述符前缀形如 (II → 参数个数粗校验 + 名字匹配
            argc = 0; k = 1
            while k < len(pre) and pre[k] != ')':
                if pre[k] == 'L': k = pre.index(';',k)+1
                else: k += 1
                argc += 1
            if argc == nparams:
                key = (name, pre); break
        if not key or key in seen: continue
        seen.add(key)
        if ' static ' in line:
            print(f'  [SKIP-static] {ifqn}.{mn}（静态方法需带体，后续处理）')
            continue
        sig = sig_of(mn, key[1])
        decls.append('    @Unique\n    public abstract ' + sig + ';')
    if not decls: continue
    body = (
        f'package {PKG};\n\n'
        f'import org.spongepowered.asm.mixin.Mixin;\n'
        f'import org.spongepowered.asm.mixin.Unique;\n\n'
        f'/**\n * Interface augmentation for {{@link {ifqn}}} (generated).\n'
        f' * Adds {len(decls)} paper-api method declaration(s); implementations live in\n'
        f' * the Craft* @Unique mixins (dev.paperarc.mixin.common.api).*\n */\n'
        f'@Mixin(targets = "{ifqn}", remap = false)\n'
        f'public interface {simple}IfaceMixin {{\n\n' + '\n\n'.join(decls) + '\n}\n')
    open(os.path.join(OUT, simple + 'IfaceMixin.java'), 'w').write(body)
    gen += 1
print(f'生成 {gen} 个接口增补 mixin -> {OUT}')
