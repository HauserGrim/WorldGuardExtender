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
        Region psel;
        try {
            psel = WEUtils.getSelection(player);
        } catch (IncompleteRegionException e) {
            return info;
        }
        BlockVector3 min = psel.getMinimumPoint();
        BlockVector3 max = psel.getMaximumPoint();
        BigInteger xWidth = BigInteger.valueOf(max.getBlockX()).subtract(BigInteger.valueOf(min.getBlockX())).add(BigInteger.ONE);
        BigInteger zWidth = BigInteger.valueOf(max.getBlockZ()).subtract(BigInteger.valueOf(min.getBlockZ())).add(BigInteger.ONE);
        BigInteger yWidth = BigInteger.valueOf(max.getBlockY()).subtract(BigInteger.valueOf(min.getBlockY())).add(BigInteger.ONE);
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

    protected class ClaimInfo {
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
