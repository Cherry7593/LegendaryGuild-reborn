package com.legendaryrealms.LegendaryGuild.Command;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandTabBuilder {

    private Set<TabList> list;
    public CommandTabBuilder(){
        list = new HashSet<>();
    }

    public CommandTabBuilder addTab(List<String> returnList,int position,List<String> previousArg,int previousPosition){
        list.add(new TabList(returnList,position,previousArg,previousPosition));
        return this;
    }

    public List<String> build(String[] args){
        List<String> returnList = new ArrayList<>();
        int length = args.length;

        if (length > 0) {
            // 获取当前正在输入的参数（最后一个参数）
            String currentInput = length > 0 ? args[length - 1] : "";

            for (TabList tabList : list) {
                if (tabList.getPosition() == length-1) {
                    // 检查前置条件
                    if (tabList.getPreviousPosition() >= length) {
                        continue;
                    }
                    if (tabList.getPreviousArg() == null){
                        continue;
                    }
                    String previousArg = args[tabList.getPreviousPosition()];

                    if (tabList.getPreviousArg().contains(previousArg)){
                        // 获取候选列表并进行前缀过滤
                        List<String> candidates = tabList.getReturnList();
                        returnList = filterByPrefix(candidates, currentInput);
                        break;
                    }
                }
            }
        }
        return returnList;
    }

    /**
     * 根据前缀过滤候选项
     * @param candidates 候选项列表
     * @param prefix 前缀（玩家当前输入的内容）
     * @return 匹配前缀的候选项列表
    **/
    private List<String> filterByPrefix(List<String> candidates, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return candidates; // 如果没有输入内容，返回所有候选项
        }

        List<String> filtered = new ArrayList<>();
        String lowerPrefix = prefix.toLowerCase(); // 不区分大小写匹配

        for (String candidate : candidates) {
            if (candidate != null && candidate.toLowerCase().startsWith(lowerPrefix)) {
                filtered.add(candidate);
            }
        }

        return filtered;
    }

    public class TabList {

        private List<String> returnList;
        //此参数出现的位置
        private int position;

        //识别上一个参数
        private List<String> previousArg;
        //上一个参数出现的位置
        private int previousPosition;

        public TabList(List<String> returnList, int position, List<String> previousArg, int previousPosition) {
            this.returnList = returnList;
            this.position = position;
            this.previousArg = previousArg;
            this.previousPosition = previousPosition;
        }

        public List<String> getReturnList() {
            return returnList;
        }

        public int getPosition() {
            return position;
        }

        public List<String> getPreviousArg() {
            return previousArg;
        }

        public int getPreviousPosition() {
            return previousPosition;
        }
    }

}
