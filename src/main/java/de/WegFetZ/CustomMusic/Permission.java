package main.java.de.WegFetZ.CustomMusic;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import org.anjocaido.groupmanager.GroupManager;

import ru.tehkode.permissions.bukkit.*;

public class Permission {

	private enum PermissionHandler {
		GROUP_MANAGER, SUPER_PERMS, PERMISSIONS_EX, NONE
	}

	private static PermissionHandler handler;
	private static Plugin permissionPlugin;

	public static void initialize(Server server) {
		Plugin permissionsBukkit = server.getPluginManager().getPlugin("PermissionsBukkit");
		Plugin permissionsEx = server.getPluginManager().getPlugin("PermissionsEx");
		Plugin bPermissions = server.getPluginManager().getPlugin("bPermissions");
		Plugin groupManager = server.getPluginManager().getPlugin("GroupManager");
		
		if (permissionsEx != null) {
			permissionPlugin = permissionsEx;
			handler = PermissionHandler.PERMISSIONS_EX;
			String version = permissionsEx.getDescription().getVersion();
			System.out.println("[CustomMusic] Permissions enabled using: PermissionsEX v" + version);
		} else if (permissionsBukkit != null) {
			permissionPlugin = permissionsBukkit;
			handler = PermissionHandler.SUPER_PERMS;
			String version = permissionsBukkit.getDescription().getVersion();
			System.out.println("[CustomMusic] Permissions enabled using: PermissionsBukkit v" + version);
		} else if (bPermissions != null) {
			permissionPlugin = bPermissions;
			handler = PermissionHandler.SUPER_PERMS;
			String version = bPermissions.getDescription().getVersion();
			System.out.println("[CustomMusic] Permissions enabled using: PermissionsBukkit v" + version);
		} 
		else if (groupManager != null) {
			permissionPlugin = groupManager;
			handler = PermissionHandler.GROUP_MANAGER;
			String version = groupManager.getDescription().getVersion();
			System.out.println("[CustomMusic] Permissions enabled using: GroupManager v" + version);
		} else {
			handler = PermissionHandler.NONE;
			System.out.println("[CustomMusic] No permissions plugin loaded.");
		}
	}


	public static boolean permission(Player player, String permission, boolean defaultPerm) {
		switch (handler) {
		case PERMISSIONS_EX:
			return PermissionsEx.getPermissionManager().has(player, permission);
		case GROUP_MANAGER:
			return ((GroupManager) permissionPlugin).getWorldsHolder().getWorldPermissions(player).has(player, permission);
		case SUPER_PERMS:
			return player.hasPermission(permission);
		case NONE:
			return player.hasPermission(permission);
		default:
			return defaultPerm;
		}
	}

	public static int getPermissionInteger(Player player, String permission, int defaultInt) {
		int max;
		switch (handler) {
		case GROUP_MANAGER:
			max = ((GroupManager) permissionPlugin).getWorldsHolder().getWorldPermissions(player).getPermissionInteger(player.getName(), permission);
			if (max == -1)
				max = defaultInt;
			return max;
		case PERMISSIONS_EX:
			max = PermissionsEx.getPermissionManager().getUser(player.getName()).getOptionInteger(permission, player.getWorld().getName(), defaultInt);
			if (max == -1)
				max = defaultInt;
			return max;
		case SUPER_PERMS:
			return defaultInt;
		case NONE:
			return defaultInt;
		default:
			return defaultInt;
		}
	}
}