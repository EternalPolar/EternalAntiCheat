package eternalpolar.spigot.eternalanticheat.Data;

import kotlin.jvm.Volatile;
import org.bukkit.Location;
import org.bukkit.util.Vector;
/**
 *  Eternal Polar 2025/7/27
 */
public class PlayerMovementData {

    @Volatile
    private Location lastLocation;
    private Vector lastVelocity = new Vector(0, 0, 0);
    private double lastDeltaY = 0.0;
    private double lastDeltaX = 0.0;
    private double lastDeltaZ = 0.0;
    private int airTicks = 0;
    private boolean onGround = false;
    private boolean wasOnGround = false;
    private boolean inLiquid = false;
    private boolean onClimbable = false;
    private boolean onSlime = false;
    private double verticalSpeed = 0.0;
    private double horizontalSpeed = 0.0;
    private double totalSpeed = 0.0;
    private int violationCount = 0;
    int consecutiveViolations = 0;
    private long lastViolationTime = 0L;
    private long fallStartTime = 0L;
    private long lastCheckTime = System.currentTimeMillis();

    private long firstAnomalyTime = 0L;

    public void updateMovementData(Location newLocation, Vector newVelocity) {
        updateDeltas(newLocation);
        updateSpeeds();
        updateTimestamps(newLocation, newVelocity);
        updateAirState();
    }

    private void updateDeltas(Location newLocation) {
        if (lastLocation != null) {
            lastDeltaX = newLocation.getX() - lastLocation.getX();
            lastDeltaY = newLocation.getY() - lastLocation.getY();
            lastDeltaZ = newLocation.getZ() - lastLocation.getZ();
        }
    }

    private void updateSpeeds() {
        verticalSpeed = Math.abs(lastDeltaY) * 20.0;
        horizontalSpeed = Math.hypot(lastDeltaX, lastDeltaZ) * 20.0;
        totalSpeed = Math.hypot(verticalSpeed, horizontalSpeed);
    }

    private void updateTimestamps(Location newLocation, Vector newVelocity) {
        lastLocation = newLocation.clone();
        lastVelocity = newVelocity.clone();
        lastCheckTime = System.currentTimeMillis();
    }

    private void updateAirState() {
        if (!onGround && !inLiquid && fallStartTime == 0L && lastDeltaY < 0.0) {
            fallStartTime = lastCheckTime;
        }
        if (!onGround) {
            airTicks++;
        }
    }

    public void setGroundState(boolean ground) {
        wasOnGround = onGround;
        onGround = ground;
        if (onGround) {
            airTicks = 0;
            fallStartTime = 0L;
        }
    }

    public void resetMovementData() {
        lastDeltaX = 0.0;
        lastDeltaY = 0.0;
        lastDeltaZ = 0.0;
        verticalSpeed = 0.0;
        horizontalSpeed = 0.0;
        totalSpeed = 0.0;
    }

    public void resetAnomalyTracking() {
        firstAnomalyTime = 0L;
    }

    public void increaseViolationCount() {
        violationCount++;
        consecutiveViolations++;
        lastViolationTime = System.currentTimeMillis();
    }

    public void resetViolationCount() {
        violationCount = 0;
        consecutiveViolations = 0;
    }

    // === 查询方法 ===
    public double getGlideRatio() {
        return totalSpeed > 0.0 ? horizontalSpeed / totalSpeed : 0.0;
    }

    public boolean isMoving() {
        return totalSpeed > 0.1;
    }

    public boolean isAscending() {
        return lastDeltaY > 0.0;
    }

    public boolean isDescending() {
        return lastDeltaY < 0.0;
    }

    public int compareTo(PlayerMovementData other) {
        return Double.compare(this.totalSpeed, other.totalSpeed);
    }

    public double getLastDeltaY() {
        return lastDeltaY;
    }

    public double getLastDeltaX() {
        return lastDeltaX;
    }

    public double getLastDeltaZ() {
        return lastDeltaZ;
    }

    public int getAirTicks() {
        return airTicks;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public Vector getLastVelocity() {
        return lastVelocity;
    }

    public void setLastLocation(Location location) {
        lastLocation = location;
    }

    public void setLastVelocity(Vector velocity) {
        lastVelocity = velocity;
    }

    public void setAirTicks(int ticks) {
        airTicks = ticks;
    }

    public int getConsecutiveViolations() {
        return consecutiveViolations;
    }

    public void setConsecutiveViolations(int count) {
        consecutiveViolations = count;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public boolean isInLiquid() {
        return inLiquid;
    }

    public void setInLiquid(boolean inLiquid) {
        this.inLiquid = inLiquid;
    }

    public boolean isOnClimbable() {
        return onClimbable;
    }

    public void setOnClimbable(boolean onClimbable) {
        this.onClimbable = onClimbable;
    }

    public boolean isOnSlime() {
        return onSlime;
    }

    public void setOnSlime(boolean onSlime) {
        this.onSlime = onSlime;
    }

    public int getViolationCount() {
        return violationCount;
    }

    public long getLastViolationTime() {
        return lastViolationTime;
    }

    public long getFallStartTime() {
        return fallStartTime;
    }

    public long getLastCheckTime() {
        return lastCheckTime;
    }

    // 新增：获取首次检测到异常的时间戳
    public long getFirstAnomalyTime() {
        return firstAnomalyTime;
    }

    // 新增：设置首次检测到异常的时间戳，在检测到异常时调用
    public void setFirstAnomalyTime(long firstAnomalyTime) {
        this.firstAnomalyTime = firstAnomalyTime;
    }
}