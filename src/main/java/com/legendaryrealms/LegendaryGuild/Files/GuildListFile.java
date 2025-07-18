package com.legendaryrealms.LegendaryGuild.Files;

import com.legendaryrealms.LegendaryGuild.LegendaryGuild;
import com.legendaryrealms.LegendaryGuild.Menu.Panels.GuildListPanel;

import java.util.List;

public class GuildListFile extends FileProvider {

    public GuildListFile(LegendaryGuild legendaryGuild) {
        super(legendaryGuild, "./plugins/LegendaryGuild/Contents/config", "Contents/config/", "GuildList.yml");
    }

    @Override
    protected void readDefault() {
        // 这里可以预加载一些默认值，如果需要的话
    }

    public GuildListPanel.Sort getDefaultSort() {
        String sortName = getValue("default_sort", "LEVEL");
        try {
            return GuildListPanel.Sort.valueOf(sortName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return GuildListPanel.Sort.LEVEL; // 如果配置的值无效，返回默认值
        }
    }

    public boolean isSortEnabled() {
        return getValue("sort.enable", true);
    }

    public List<String> getAvailableSorts() {
        return getValue("sort.available_sorts", List.of("LEVEL", "MEMBERS", "ACTIVITY", "TREELEVEL", "MONEY", "DEFAULT"));
    }

    public boolean isShowSortButton() {
        return getValue("display.show_sort_button", true);
    }

    public boolean isShowPageButtons() {
        return getValue("display.show_page_buttons", true);
    }

    public void reload() {
        // 重新加载配置文件
        try {
            yml.load(file);
        } catch (Exception e) {
            legendaryGuild.getLogger().severe("重新加载GuildList.yml失败: " + e.getMessage());
        }
    }
}
