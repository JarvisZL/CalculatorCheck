package com.example.projectone;

import android.util.Log;
import android.util.Pair;


import java.util.ArrayList;
import java.util.List;

public class Calculator {


    private static int calculateleft(List<Pair<String,String>> items){
        //用MIN_VALUE作为格式出错的标识码，有待改善
        if(items.size() == 0){
            return Integer.MIN_VALUE;
        }
        if(items.size() == 1){
            if(items.get(0).second.equals("op"))
                return Integer.MIN_VALUE;
            return Integer.valueOf(items.get(0).first);
        }

        int multiindex = -1,divindex = -1;
        //从右到左遍历寻找优先级最低的运算符
        //递归求解
        for(int i = items.size() - 1; i >= 0; --i){
            if(items.get(i).second.equals("op")){
                if(items.get(i).first.equals("+")){
                    return calculateleft(items.subList(0,i)) + calculateleft(items.subList(i+1,items.size()));
                }
                else if(items.get(i).first.equals("-")){
                    return calculateleft(items.subList(0,i)) - calculateleft(items.subList(i+1,items.size()));
                }
                else if(items.get(i).first.equals("*")){
                    multiindex = multiindex == -1 ? i : multiindex;
                }
                else if(items.get(i).first.equals("/")){
                    divindex = divindex == -1 ? i : divindex;
                }
                else{
                    assert false: "No such a operator";
                }
            }
        }
        //*号在更右边，优先
        if(multiindex > divindex){
            return calculateleft(items.subList(0,multiindex)) * calculateleft(items.subList(multiindex+1,items.size()));
        }
        else{
            return calculateleft(items.subList(0,divindex)) / calculateleft(items.subList(divindex+1,items.size()));
        }

    }

    private static String calculate(List<Pair<String,String>> items){
        for(int i = 0; i < items.size(); ++i){
            //'='号
            if(items.get(i).second.equals("op") && items.get(i).first.equals("=")){
                //'='号已经是这个算式中的最后一个项，说明没有结果
                if(i == items.size() - 1){
                    return "Missing answer";
                }
                //'='号右边的不是数字，不符合规定的格式
                if(i == items.size() - 2 && !items.get(i+1).second.equals("num")){
                    return "Not a equation";
                }
                //rightans存储了等号右边的数字的值，防止出现由于识别错误导致溢出行为，使用Long
                long rightans = Long.valueOf(items.get(i+1).first);
                //超过假设的最大值，结果错误
                if(rightans > Integer.MAX_VALUE)
                    return "Wrong";
                //将等号左侧的部分复制到leftitems
                List<Pair<String,String>> leftitems = new ArrayList<>();
                for(int j = 0; j < i; ++j){
                    leftitems.add(items.get(j));
                }
                //调用calculateleft函数计算
                int leftans = calculateleft(leftitems);
                if(leftans == Integer.MIN_VALUE)
                    return "Not a equation";
                if(leftans == rightans)
                    return "Right";
                else
                    return "Wrong";
            }
        }
        //没有识别出'='号，不符合规定格式
        return "Not a equation";
    }

    private static String getAns(String text){
        int len = text.length();
        //用来记录识别出来的每一个算式中各个字符的类型，从而拼接。
        //数字为pair<numbervalue,"num">
        //运算符为pair<operator,"op">
        List<Pair<String,String>> items = new ArrayList<>();
        int index;
        //目前的字符
        char c;
        StringBuilder str;
        boolean is_num, lastflag;
        //遍历
        for(index = 0; index < len;){
            c = text.charAt(index);
            str = new StringBuilder();
            //is_num用于标记是否是数字
            is_num = false;
            //lastflag用于标记是否已经到了结尾
            lastflag = false;

            while(c >= 48 && c <= 57){
                is_num = true;
                str.append(c);
                index++;
                //已经到了当前算式末尾
                if(index >= len){
                    lastflag = true;
                    break;
                }
                c = text.charAt(index);
            }
            if(is_num) {
                items.add(new Pair<>(String.valueOf(str), "num"));
            }
            if(!lastflag){
                items.add(new Pair<>(String.valueOf(c),"op"));
            }
            index++;
        }

        //拼接完成后调用calculate计算
        return Calculator.calculate(items);
    }

    //提供的接口
    public static String Check(String text){
        if(text.equals("")) return "";
        return Calculator.getAns(text);
    }
}
