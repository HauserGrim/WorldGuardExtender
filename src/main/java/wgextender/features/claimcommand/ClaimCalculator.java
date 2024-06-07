package wgextender.features.claimcommand;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.entity.Player;
import wgextender.Config;
import wgextender.VaultIntegration;
import wgextender.utils.WEUtils;

import java.math.BigInteger;

public class ClaimCalculator {

    public ClaimInfo getClaimInfo(Config config, Player player) {
        ClaimInfo info = new ClaimInfo();
        Region selection;
        try {
            selection = WEUtils.getSelection(player);
        } catch (IncompleteRegionException e) {
            return info;
        }
        BlockVector3 min = selection.getMinimumPoint();
        BlockVector3 max = selection.getMaximumPoint();
        BigInteger xWidth = distance(min.getBlockX(), max.getBlockX());
        BigInteger zWidth = distance(min.getBlockZ(), max.getBlockZ());
        BigInteger yWidth = distance(min.getBlockY(), max.getBlockY());
        BigInteger size = xWidth.multiply(zWidth).multiply(yWidth);
        BigInteger minw = xWidth.min(zWidth).min(yWidth);
        String[] pgroups = VaultIntegration.getInstance().getPermissions().getPlayerGroups(player);
        int maxsize = 0;
        if (pgroups.length > 0) {
            for (String pgroup : pgroups) {
                pgroup = pgroup.toLowerCase();
                if (config.claimBlockLimins.containsKey(pgroup)) {
                    maxsize = Math.max(maxsize, config.claimBlockLimins.get(pgroup));
                }
            }
        }
        info.setInfo(size, BigInteger.valueOf(maxsize), minw);
        return info;
    }

    private BigInteger distance(long min, long max) {
        return BigInteger.valueOf(max - min + 1L);
    }

    protected static class ClaimInfo {
        private BigInteger size;
        private BigInteger maxsize;
        private BigInteger minwidthsize;

        public void setInfo(BigInteger size, BigInteger maxsize, BigInteger minwidthsize) {
            this.size = size;
            this.maxsize = maxsize;
            this.minwidthsize = minwidthsize;
        }

        public BigInteger getSize() {
            return this.size;
        }

        public BigInteger getMaxsize() {
            return this.maxsize;
        }

        public BigInteger getMinWidthSize() {
            return this.minwidthsize;
        }
    }
}
