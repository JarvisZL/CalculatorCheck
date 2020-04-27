package com.example.projectone;

import android.util.Log;
import android.util.Pair;


import java.util.ArrayList;
import java.util.List;

public class Calculator {


    private static int calculateleft(List<Pair<String,String>> items){
        if(items.size() == 0){
            return Integer.MIN_VALUE;
        }
        if(items.size() == 1){
            if(items.get(0).second.equals("op"))
                return Integer.MIN_VALUE;
            return Integer.valueOf(items.get(0).first);
        }

        int multiindex = -1,divindex = -1;
        //从右到左遍历寻找运算符
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
        if(multiindex > divindex){
            return calculateleft(items.subList(0,multiindex)) * calculateleft(items.subList(multiindex+1,items.size()));
        }
        else{
            return calculateleft(items.subList(0,divindex)) / calculateleft(items.subList(divindex+1,items.size()));
        }

    }

    private static String calculate(List<Pair<String,String>> items){
        for(int i = 0; i < items.size(); ++i){
            if(items.get(i).second.equals("op") && items.get(i).first.equals("=")){
                if(i == items.size() - 1){
                    return "Missing answer";
                }
                if(!items.get(i+1).second.equals("num")){
                    return "Unqualified";
                }

                int rightans = Integer.valueOf(items.get(i+1).first);
                List<Pair<String,String>> leftitems = new ArrayList<>();
                for(int j = 0; j < i; ++j){
                    leftitems.add(items.get(j));
                }
                int leftans = calculateleft(leftitems);
                if(leftans == Integer.MIN_VALUE)
                    return "Unqualified";
                if(leftans == rightans)
                    return "Right";
                else
                    return "Wrong";
            }
        }
        return "Unqualified";
    }

    private static String simplecalculate(List<Pair<String,String>> items){
        //simple demo
        int ans = 0,cur;
        String curop = null,oldop = null;
        boolean ansflag = false,missingflag = false;
        for(int i = 0; i < items.size(); ++i){
            if(i == 0){
                if(items.get(i).second.equals("op"))
                    break;
                ans = Integer.valueOf(items.get(i).first);
            }
            else{
                String type = items.get(i).second;
                if(type.equals("num")){
                    oldop = null;
                    cur = Integer.valueOf(items.get(i).first);
                    if(curop == null)
                        break;
                    if(curop.equals("+"))
                        ans = ans + cur;
                    else if(curop.equals("-"))
                        ans = ans - cur;
                    else if(curop.equals("*"))
                        ans = ans * cur;
                    else if(curop.equals("/"))
                        ans = ans / cur;
                    else if(curop.equals("=")){
                        ansflag = ans == cur;
                        break;
                    }

                }
                else if(type.equals("op")){
                    if(oldop != null)
                        break;
                    oldop = curop;
                    curop = items.get(i).first;
                    if(curop.equals("=") && i == items.size() - 1){
                            missingflag = true;
                            break;
                    }
                }
            }
        }
        if(missingflag)
            return "Missing answer";
        if(ansflag)
            return "Right";
        else
            return "Wrong";
    }

    private static String getAns(String text){
        int len = text.length();
        List<Pair<String,String>> items = new ArrayList<>();
        int index;
        char c;
        StringBuilder str;
        boolean flag, lastflag;
        for(index = 0; index < len;){
            c = text.charAt(index);
            str = new StringBuilder();
            flag = false;
            lastflag = false;
            while(c >= 48 && c <= 57){
                flag = true;
                str.append(c);
                index++;
                if(index >= len){
                    lastflag = true;
                    break;
                }
                c = text.charAt(index);
            }
            if(flag) {
                items.add(new Pair<>(String.valueOf(str), "num"));
            }
            if(!lastflag){
                items.add(new Pair<>(String.valueOf(c),"op"));
            }
            index++;
        }
        return Calculator.calculate(items);
    }

    public static String Check(String text){
        if(text.equals("")) return "";
        return Calculator.getAns(text);
    }
}
