package com.ixnah.mc.paperarc.bridge;

import com.google.common.base.Preconditions;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;

import java.util.ArrayList;
import java.util.List;

/**
 * Paper Mob Pathfinding API 的 Pathfinder 实现（对应 Paper 的
 * com.destroystokyo.paper.entity.PaperPathfinder），内部委托 NMS
 * {@link PathNavigation}。由 CraftMobApiMixin#.getPathfinder() 按宿主实例缓存。
 *
 * 与 Paper 原版的差异：NMS Path#nodes 为私有字段且本环境无 Paper 的 AT 放开，
 * 遍历路径点改用公开的 getNode(int)/getNodeCount()；Location 世界的获取改走
 * PaperArcBridge#bukkitWorld(ServerLevel)（编译期 NMS 无 Level#getWorld）。
 */
public class PaperPathfinder implements com.destroystokyo.paper.entity.Pathfinder {

    private final net.minecraft.world.entity.Mob entity;

    public PaperPathfinder(net.minecraft.world.entity.Mob entity) {
        this.entity = entity;
    }

    @Override
    public Mob getEntity() {
        return (Mob) PaperArcBridge.bukkitEntity(entity);
    }

    private PathNavigation navigation() {
        return entity.getNavigation();
    }

    @Override
    public void stopPathfinding() {
        navigation().stop();
    }

    @Override
    public boolean hasPath() {
        Path path = navigation().getPath();
        return path != null && !path.isDone();
    }

    @Override
    public PathResult getCurrentPath() {
        Path path = navigation().getPath();
        return path != null && !path.isDone() ? new PaperPathResult(path) : null;
    }

    @Override
    public PathResult findPath(Location loc) {
        Preconditions.checkNotNull(loc, "Location can not be null");
        Path path = navigation().createPath(loc.getX(), loc.getY(), loc.getZ(), 0);
        return path != null ? new PaperPathResult(path) : null;
    }

    @Override
    public PathResult findPath(LivingEntity target) {
        Preconditions.checkNotNull(target, "Target can not be null");
        Path path = navigation().createPath(((CraftLivingEntity) target).getHandle(), 0);
        return path != null ? new PaperPathResult(path) : null;
    }

    @Override
    public boolean moveTo(PathResult pathResult, double speed) {
        Preconditions.checkNotNull(pathResult, "PathResult can not be null");
        if (!(pathResult instanceof PaperPathResult result)) {
            throw new IllegalArgumentException("Foreign PathResult implementation: " + pathResult.getClass());
        }
        return navigation().moveTo(result.path, speed);
    }

    // NodeEvaluator 门/浮水能力，与 Paper 一致委托 navigation.nodeEvaluator。

    @Override
    public boolean canOpenDoors() {
        return navigation().getNodeEvaluator().canOpenDoors();
    }

    @Override
    public void setCanOpenDoors(boolean canOpenDoors) {
        navigation().getNodeEvaluator().setCanOpenDoors(canOpenDoors);
    }

    @Override
    public boolean canPassDoors() {
        return navigation().getNodeEvaluator().canPassDoors();
    }

    @Override
    public void setCanPassDoors(boolean canPassDoors) {
        navigation().getNodeEvaluator().setCanPassDoors(canPassDoors);
    }

    @Override
    public boolean canFloat() {
        return navigation().canFloat();
    }

    @Override
    public void setCanFloat(boolean canFloat) {
        navigation().setCanFloat(canFloat);
    }

    private Location toLoc(Node point) {
        return new Location(PaperArcBridge.bukkitWorld(
                (net.minecraft.server.level.ServerLevel) entity.level()), point.x, point.y, point.z);
    }

    public class PaperPathResult implements com.destroystokyo.paper.entity.Pathfinder.PathResult {

        private final Path path;

        PaperPathResult(Path path) {
            this.path = path;
        }

        @Override
        public Location getFinalPoint() {
            Node point = path.getEndNode();
            return point != null ? toLoc(point) : null;
        }

        @Override
        public boolean canReachFinalPoint() {
            return path.canReach();
        }

        @Override
        public List<Location> getPoints() {
            List<Location> points = new ArrayList<>();
            for (int i = 0; i < path.getNodeCount(); i++) {
                points.add(toLoc(path.getNode(i)));
            }
            return points;
        }

        @Override
        public int getNextPointIndex() {
            return path.getNextNodeIndex();
        }

        @Override
        public Location getNextPoint() {
            int index = path.getNextNodeIndex();
            if (index >= path.getNodeCount()) {
                return null;
            }
            return toLoc(path.getNode(index));
        }
    }
}
