/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 */

package wgextender.features.claimcommand;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.worldedit.blocks.BaseItemStack;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.function.mask.Mask;
import com.sk89q.worldedit.internal.cui.CUIEvent;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldedit.util.HandSide;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.util.formatting.text.Component;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import com.sk89q.worldedit.world.gamemode.GameMode;
import com.sk89q.worldedit.world.weather.WeatherType;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.commands.region.RegionCommands;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import wgextender.Config;
import wgextender.utils.CommandUtils;
import wgextender.utils.WEUtils;
import wgextender.utils.WGRegionUtils;

import java.io.File;
import java.math.BigInteger;
import java.util.Locale;
import java.util.UUID;

public class WGRegionCommandWrapper extends Command {

	public static void inject(Config config) throws IllegalAccessException {
		WGRegionCommandWrapper wrapper = new WGRegionCommandWrapper(config, CommandUtils.getCommands().get("region"));
		CommandUtils.replaceComamnd(wrapper.originalcommand, wrapper);
	}

	public static void uninject() throws IllegalAccessException {
		WGRegionCommandWrapper wrapper = (WGRegionCommandWrapper) CommandUtils.getCommands().get("region");
		CommandUtils.replaceComamnd(wrapper, wrapper.originalcommand);
	}

	protected final Config config;
	protected final Command originalcommand;
	protected static final RegionCommands regionCommands = new RegionCommands(WorldGuard.getInstance());
	private final ClaimCalculator claimCalculator = new ClaimCalculator();

	protected WGRegionCommandWrapper(Config config, Command originalcommand) {
		super(originalcommand.getName(), originalcommand.getDescription(), originalcommand.getUsage(), originalcommand.getAliases());
		this.config = config;
		this.originalcommand = originalcommand;
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if ((sender instanceof Player player) && (args.length >= 2) && args[0].equalsIgnoreCase("claim")) {
			String regionname = args[1];
			if (config.claimExpandSelectionVertical) {
				boolean result = WEUtils.expandVert((Player) sender);
				if (result) {
					player.sendMessage(ChatColor.GRAY + "Регион автоматически расширен по вертикали.");
				}
			}
			if (!player.hasPermission("worldguard.region.unlimited")) {
				ClaimCalculator.ClaimInfo info = claimCalculator.getClaimInfo(config, player);
				BigInteger size = info.getSize();
				if (config.claimBlockLimitsEnabled) {
					BigInteger maxsize = info.getMaxsize();
					if (size.compareTo(maxsize) > 0) {
						player.sendMessage(ChatColor.RED + "Вы не можете создать такой большой регион.");
						player.sendMessage(ChatColor.RED + "Ваш лимит: " + maxsize + ", Ваш размер: " + size);
						return true;
					}
				}
				int minsize = config.claimMinSize;
				if (minsize > 0) {
					if (size.compareTo(BigInteger.valueOf(minsize)) < 0) {
						player.sendMessage(ChatColor.RED + "Вы не можете создать такой маленький регион.");
						player.sendMessage(ChatColor.RED + "Минимальный размер: " + minsize + ", Ваш размер: " + size);
						return true;
					}
				}
				int minwidth = config.claimMinWidth;
				if (minwidth > 0) {
					BigInteger width = info.getMinWidthSize();
					if (width.compareTo(BigInteger.valueOf(minwidth)) < 0) {
						player.sendMessage( "Вы не можете создать такой узкий регион.");
						player.sendMessage(ChatColor.RED + "Вы не можете создать такой узкий регион.");
						player.sendMessage(ChatColor.RED + "Миниммальная ширина: " + minwidth + ", Ваша ширина: " + width);
						return true;
					}
				}
			}
			boolean hasRegion = AutoFlags.hasRegion(player.getWorld(), regionname);
			try {
				CommandContext ccontext = new CommandContext(String.format("claim %s", regionname));
				regionCommands.claim(ccontext, WGRegionUtils.wrapPlayer(player));
				if (!hasRegion && config.claimAutoFlagsEnabled) {
					final FakeConsoleSender fakePlayerSender = new FakeConsoleSender(player);
					AutoFlags.setFlagsForRegion(fakePlayerSender, player.getWorld(), config, regionname);
				}
			} catch (CommandException ex) {
				sender.sendMessage(ChatColor.RED + ex.getMessage());
			}
			return true;
		} else {
			return originalcommand.execute(sender, label, args);
		}
	}



	private record FakeConsoleSender(Player player) implements LocalPlayer, Actor {

		@Override
		public boolean ascendLevel() {
			return false;
		}

		@Override
		public boolean ascendToCeiling(final int arg0) {
			return false;
		}

		@Override
		public boolean ascendToCeiling(final int arg0, final boolean arg1) {
			return false;
		}

		@Override
		public boolean ascendUpwards(final int arg0) {
			return false;
		}

		@Override
		public boolean ascendUpwards(final int arg0, final boolean arg1) {
			return false;
		}

		@Override
		public boolean descendLevel() {
			return false;
		}

		@Override
		public void findFreePosition() {
		}

		@Override
		public void findFreePosition(final Location arg0) {
		}

		@Override
		public void floatAt(final int arg0, final int arg1, final int arg2, final boolean arg3) {
		}

		@Override
		public BaseBlock getBlockInHand(final HandSide arg0) {
			return null;
		}

		@Override
		public Location getBlockOn() {
			return null;
		}

		@Override
		public Location getBlockTrace(final int arg0) {
			return null;
		}

		@Override
		public Location getBlockTrace(final int arg0, final boolean arg1) {
			return null;
		}

		@Override
		public Location getBlockTrace(final int arg0, final boolean arg1, final Mask arg2) {
			return null;
		}

		@Override
		public Location getBlockTraceFace(final int arg0, final boolean arg1) {
			return null;
		}

		@Override
		public Location getBlockTraceFace(final int arg0, final boolean arg1, final Mask arg2) {
			return null;
		}

		@Override
		public Direction getCardinalDirection() {
			return null;
		}

		@Override
		public Direction getCardinalDirection(final int arg0) {
			return null;
		}

		@Override
		public GameMode getGameMode() {
			return null;
		}

		@Override
		public BlockBag getInventoryBlockBag() {
			return null;
		}

		@Override
		public BaseItemStack getItemInHand(final HandSide arg0) {
			return null;
		}

		@Override
		public Location getSolidBlockTrace(final int arg0) {
			return null;
		}

		@Override
		public World getWorld() {
			return BukkitAdapter.adapt(player.getWorld());
		}

		@Override
		public void giveItem(final BaseItemStack arg0) {
		}

		@Override
		public boolean isHoldingPickAxe() {
			return false;
		}

		@Override
		public boolean passThroughForwardWall(final int arg0) {
			return false;
		}

		@Override
		public <B extends BlockStateHolder<B>> void sendFakeBlock(final BlockVector3 arg0, final B arg1) {
		}

		@Override
		public void setGameMode(final GameMode arg0) {
		}

		@Override
		public void setOnGround(final Location arg0) {
		}

		@Override
		public BaseEntity getState() {
			return null;
		}

		@Override
		public boolean remove() {
			return false;
		}

		@Override
		public <T> T getFacet(final Class<? extends T> arg0) {
			return null;
		}

		@Override
		public Extent getExtent() {
			return null;
		}

		@Override
		public Location getLocation() {
			return BukkitAdapter.adapt(player.getLocation());
		}

		@Override
		public boolean setLocation(final Location arg0) {
			return false;
		}

		@Override
		public boolean canDestroyBedrock() {
			return false;
		}

		@Override
		public void dispatchCUIEvent(final CUIEvent arg0) {
		}

		@Override
		public Locale getLocale() {
			return null;
		}

		@Override
		public String getName() {
			return player.getName();
		}

		@Override
		public boolean isPlayer() {
			return false;
		}

		@Override
		public File openFileOpenDialog(final String[] arg0) {
			return null;
		}

		@Override
		public File openFileSaveDialog(final String[] arg0) {
			return null;
		}

		@Override
		public void print(final String arg0) {
		}

		@Override
		public void print(final Component arg0) {
		}

		@Override
		public void printDebug(final String arg0) {
		}

		@Override
		public void printError(final String arg0) {
		}

		@Override
		public void printRaw(final String arg0) {
		}

		@Override
		public UUID getUniqueId() {
			return player.getUniqueId();
		}

		@Override
		public SessionKey getSessionKey() {
			return null;
		}

		@Override
		public void checkPermission(final String arg0) {
		}

		@Override
		public String[] getGroups() {
			return null;
		}

		@Override
		public boolean hasPermission(final String arg0) {
			return true;
		}

		@Override
		public void ban(final String arg0) {
		}

		@Override
		public float getExhaustion() {
			return 0.0f;
		}

		@Override
		public int getFireTicks() {
			return 0;
		}

		@Override
		public double getFoodLevel() {
			return 0.0;
		}

		@Override
		public double getHealth() {
			return 0.0;
		}

		@Override
		public double getMaxHealth() {
			return 0.0;
		}

		@Override
		public long getPlayerTimeOffset() {
			return 0L;
		}

		@Override
		public WeatherType getPlayerWeather() {
			return null;
		}

		@Override
		public double getSaturation() {
			return 0.0;
		}

		@Override
		public boolean hasGroup(final String arg0) {
			return false;
		}

		@Override
		public boolean isPlayerTimeRelative() {
			return true;
		}

		@Override
		public void kick(final String arg0) {
		}

		@Override
		public void resetFallDistance() {
		}

		@Override
		public void resetPlayerTime() {
		}

		@Override
		public void resetPlayerWeather() {
		}

		@Override
		public void sendTitle(final String arg0, final String arg1) {
		}

		@Override
		public void setCompassTarget(final Location arg0) {
		}

		@Override
		public void setExhaustion(final float arg0) {
		}

		@Override
		public void setFireTicks(final int arg0) {
		}

		@Override
		public void setFoodLevel(final double arg0) {
		}

		@Override
		public void setHealth(final double arg0) {
		}

		@Override
		public void setPlayerTime(final long arg0, final boolean arg1) {
		}

		@Override
		public void setPlayerWeather(final WeatherType arg0) {
		}

		@Override
		public void setSaturation(final double arg0) {
		}

		@Override
		public void teleport(final Location arg0, final String arg1, final String arg2) {
		}
	}
}
