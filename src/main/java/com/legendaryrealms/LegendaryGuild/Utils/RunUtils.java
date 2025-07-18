package com.legendaryrealms.LegendaryGuild.Utils;

import com.legendaryrealms.LegendaryGuild.API.GuildAPI;
import com.legendaryrealms.LegendaryGuild.API.UserAPI;
import com.legendaryrealms.LegendaryGuild.Command.AdminCommands.ActivityCommand;
import com.legendaryrealms.LegendaryGuild.Data.Guild.Guild;
import com.legendaryrealms.LegendaryGuild.Data.User.User;
import com.legendaryrealms.LegendaryGuild.LegendaryGuild;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunUtils {

    private List<String> runs;
    private Player p;

    public RunUtils(List<String> runs, Player p) {
        this.runs = runs;
        this.p = p;
    }

    public void start() {
        runs.forEach(s -> {
            deal(s);
        });
    }

    private void deal(String run){
        if (run.startsWith("[")){
            String tag = getSymbol(run);
            String dealStr = LegendaryGuild.getInstance().color(run.replace("["+tag+"]","").replace("%player%",p.getName()));
            double chance = getChance(dealStr);
            if (chance != -1){
                dealStr=dealStr.replace("~chance:"+chance,"");
            }
            chance = ((chance==-1) ? 1 : chance);

            // 为了Folia兼容性，将最终的dealStr设为final
            final String finalDealStr = dealStr;
            final double finalChance = chance;

            switch (tag) {
                case "player":
                    if ((new Random()).nextInt(101) <= finalChance*100){
                        // 在主线程中执行玩家命令
                        LegendaryGuild.getInstance().sync(() -> p.performCommand(finalDealStr));
                    }
                    break;
                case "op":
                    if ((new Random()).nextInt(101) <= finalChance*100){
                        // 在主线程中执行OP命令
                        LegendaryGuild.getInstance().sync(() -> {
                            if (p.isOp()){
                                p.performCommand(finalDealStr);
                                return;
                            }
                            p.setOp(true);
                            p.performCommand(finalDealStr);
                            p.setOp(false);
                        });
                    }
                    break;
                case "console":
                    if ((new Random()).nextInt(101) <= finalChance*100){
                        // 在主线程中执行控制台命令
                        LegendaryGuild.getInstance().sync(() ->
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalDealStr)
                        );
                    }
                    break;
                case "message":
                    if ((new Random()).nextInt(101) <= finalChance*100){
                        // 发送消息可以在任何线程执行
                        p.sendMessage(finalDealStr);
                    }
                    break;
                case "title":
                    if ((new Random()).nextInt(101) <= finalChance*100){
                        String[] title = finalDealStr.split(";");
                        String main = getFromArray(title,0,"");
                        String sub = getFromArray(title,1,"");
                        // 在主线程中发送标题
                        LegendaryGuild.getInstance().sync(() -> p.sendTitle(main, sub));
                    }
                    break;
                case "sound":
                    if ((new Random()).nextInt(101) <= finalChance*100){
                        String[] args = finalDealStr.split(";");
                        String sound = getFromArray(args,0,"BLOCK_CHEST_OPEN");
                        int pitch = getFromArray(args,1,1);
                        int volume = getFromArray(args,2,1);
                        // 在主线程中播放声音
                        LegendaryGuild.getInstance().sync(() ->
                            p.playSound(p.getLocation(), Sound.valueOf(sound), pitch, volume)
                        );
                    }
                    break;
                case "broad":
                    if ((new Random()).nextInt(101) <= finalChance*100){
                        // 广播消息可能涉及所有玩家，在主线程执行
                        LegendaryGuild.getInstance().sync(() ->
                            LegendaryGuild.getInstance().getMsgUtils().sendBroad(finalDealStr)
                        );
                    }
                    break;
                case "broad_guild":
                    if ((new Random()).nextInt(101) <= finalChance*100){
                        User user = UserAPI.getUser(p.getName());
                        if (user.hasGuild()){
                            Guild guild = LegendaryGuild.getInstance().getGuildsManager().getGuild(user.getGuild());
                            // 公会广播在主线程执行
                            LegendaryGuild.getInstance().sync(() ->
                                LegendaryGuild.getInstance().getMsgUtils().sendGuildMessage(guild.getMembers(), finalDealStr)
                            );
                        }
                    }
                    break;
                case "guild_money":
                    if ((new Random()).nextInt(101) <= finalChance*100){
                        User user = UserAPI.getUser(p.getName());
                        if (user.hasGuild()) {
                            Guild guild = LegendaryGuild.getInstance().getGuildsManager().getGuild(user.getGuild());
                            double amount = Double.parseDouble(finalDealStr);
                            // 数据库操作可以异步，但为了一致性在主线程执行
                            LegendaryGuild.getInstance().sync(() -> {
                                guild.addMoney(amount);
                                guild.update();
                            });
                        }
                    }
                    break;
                case "guild_points":
                    if ((new Random()).nextInt(101) <= finalChance*100) {
                        User user = UserAPI.getUser(p.getName());
                        if (user.hasGuild()) {
                            // 数据库操作可以异步，但为了一致性在主线程执行
                            LegendaryGuild.getInstance().sync(() -> {
                                user.addPoints(Double.parseDouble(finalDealStr), true);
                                user.update(false);
                            });
                        }
                    }
                    break;
                case "guild_activity":
                    if ((new Random()).nextInt(101) <= finalChance*100) {
                        User user = UserAPI.getUser(p.getName());
                        if (user.hasGuild()) {
                            // 在主线程中执行公会活动添加
                            LegendaryGuild.getInstance().sync(() ->
                                GuildAPI.addGuildActivity(p, null, Double.parseDouble(finalDealStr), ActivityCommand.AddType.PLAYER)
                            );
                        }
                    }
                    break;
                case "guild_exp" :
                    User user = UserAPI.getUser(p.getName());
                    if (user.hasGuild()) {
                        if (UserAPI.getGuild(p.getName()).isPresent()) {
                            // 在主线程中执行公会经验添加
                            LegendaryGuild.getInstance().sync(() ->
                                GuildAPI.addGuildExp(p.getName(), UserAPI.getGuild(p.getName()).get(), Double.parseDouble(finalDealStr))
                            );
                        }
                    }
                    break;
            }
            return;
        }
        LegendaryGuild.getInstance().info("配置格式出错:必须以[标识]开头。比如[console]xxxx -> "+run, Level.SEVERE);
    }

    private <T> T getFromArray(String[] str,int pos,T def){
        if (str != null && str.length > pos) {
            return (T)str[pos];
        }
        return def;
    }

    private String getSymbol(String str){
        StringBuilder builder = new StringBuilder();
        boolean begin = false;
        for (char c : str.toCharArray()){
            if (begin){
                if (c == ']'){
                    break;
                }
                builder.append(c);
            }
            else {
                if (c == '['){
                    begin = true;
                }
            }
        }
        return builder.toString();
    }

    private double getChance(String str){
        if (str.contains("~chance:")){
            Pattern pattern = Pattern.compile("~chance:(\\d+(?:\\.\\d+)?)");
            Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                return Double.parseDouble(matcher.group(1));
            }
        }
        return -1;
    }
}
