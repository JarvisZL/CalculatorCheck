package com.example.projectone;

import android.util.Pair;


import java.util.ArrayList;
import java.util.List;

public class Calculator {


    private static String calculate(List<Pair<String,String>> items){
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
