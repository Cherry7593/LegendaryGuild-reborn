package com.legendaryrealms.LegendaryGuild.Manager;

import com.legendaryrealms.LegendaryGuild.Command.Commands;
import com.legendaryrealms.LegendaryGuild.LegendaryGuild;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.logging.Level;

/**
 * 动态指令管理器
 * 负责根据配置文件动态注册指令别名
 */
public class CommandManager {

    private final LegendaryGuild plugin;
    private CommandMap commandMap;

    public CommandManager(LegendaryGuild plugin) {
        this.plugin = plugin;
        this.initCommandMap();
    }

    /**
     * 初始化CommandMap
     */
    private void initCommandMap() {
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (CommandMap) commandMapField.get(Bukkit.getServer());
        } catch (Exception e) {
            plugin.info("无法获取CommandMap，将使用默认指令注册方式", Level.WARNING);
        }
    }

    /**
     * 注册插件指令和别名
     */
    public void registerCommands() {
        Commands commands = new Commands();

        // 注册主指令
        PluginCommand mainCommand = Bukkit.getPluginCommand("legendaryguild");
        if (mainCommand != null) {
            mainCommand.setExecutor(commands);
            mainCommand.setTabCompleter(commands);
        }

        // 如果启用了自定义别名且能获取到CommandMap，则注册动态别名
        if (plugin.getFileManager().getConfig().ENABLE_CUSTOM_ALIASES && commandMap != null) {
            registerDynamicAliases(commands);
        }

        // 注册子命令
        Commands.register();

        plugin.info("指令注册完成", Level.INFO);
    }

    /**
     * 注册动态别名
     */
    private void registerDynamicAliases(Commands commands) {
        List<String> aliases = plugin.getFileManager().getConfig().COMMAND_ALIASES;

        if (aliases == null || aliases.isEmpty()) {
            plugin.info("未配置指令别名，跳过动态别名注册", Level.INFO);
            return;
        }

        for (String alias : aliases) {
            if (alias == null || alias.trim().isEmpty()) {
                continue;
            }

            alias = alias.trim().toLowerCase();

            // 检查别名是否已存在
            if (commandMap.getCommand(alias) != null) {
                plugin.info("指令别名 '" + alias + "' 已存在，跳过注册", Level.WARNING);
                continue;
            }

            try {
                // 创建别名指令
                PluginCommand aliasCommand = createPluginCommand(alias, plugin);
                if (aliasCommand != null) {
                    aliasCommand.setExecutor(commands);
                    aliasCommand.setTabCompleter(commands);

                    // 注册到CommandMap
                    commandMap.register(plugin.getDescription().getName().toLowerCase(), aliasCommand);
                    plugin.info("成功注册指令别名: /" + alias, Level.INFO);
                }
            } catch (Exception e) {
                plugin.info("注册指令别名 '" + alias + "' 失败: " + e.getMessage(), Level.WARNING);
            }
        }
    }

    /**
     * 创建PluginCommand实例
     */
    private PluginCommand createPluginCommand(String name, LegendaryGuild plugin) {
        try {
            Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, org.bukkit.plugin.Plugin.class);
            constructor.setAccessible(true);
            return constructor.newInstance(name, plugin);
        } catch (Exception e) {
            plugin.info("创建PluginCommand失败: " + e.getMessage(), Level.WARNING);
            return null;
        }
    }

    /**
     * 获取当前配置的别名列表
     */
    public List<String> getConfiguredAliases() {
        return plugin.getFileManager().getConfig().COMMAND_ALIASES;
    }

    /**
     * 检查自定义别名是否启用
     */
    public boolean isCustomAliasesEnabled() {
        return plugin.getFileManager().getConfig().ENABLE_CUSTOM_ALIASES;
    }

    /**
     * 重新加载指令配置（需要重启服务器才能完全生效）
     */
    public void reloadCommandConfig() {
        plugin.info("指令配置已重新加载，部分更改需要重启服务器才能生效", Level.INFO);

        if (isCustomAliasesEnabled()) {
            List<String> aliases = getConfiguredAliases();
            plugin.info("当前配置的指令别名: " + String.join(", ", aliases), Level.INFO);
        } else {
            plugin.info("自定义指令别名已禁用", Level.INFO);
        }
    }
}
