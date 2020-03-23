package com.example.projectone;

import android.util.Pair;


import java.util.ArrayList;
import java.util.List;

public class Calculator {


    private static String calculate(List<Pair<String,String>> items){
        //simple demo
        int ans = 0,cur;
        String op = null;
        boolean ansflag = false,missingflag = false;
        for(int i = 0; i < items.size(); ++i){
            if(i == 0)
                ans = Integer.valueOf(items.get(i).first);
            else{
                String type = items.get(i).second;
                if(type.equals("num")){
                    cur = Integer.valueOf(items.get(i).first);
                    if(op == null)
                        throw new AssertionError("Missing op");
                    if(op.equals("+"))
                        ans = ans + cur;
                    else if(op.equals("-"))
                        ans = ans - cur;
                    else if(op.equals("*"))
                        ans = ans * cur;
                    else if(op.equals("/"))
                        ans = ans / cur;
                    else if(op.equals("=")){
                        ansflag = ans == cur;
                        break;
                    }

                }
                else if(type.equals("op")){
                    op = items.get(i).first;
                    if(op.equals("=") && i == items.size() - 1){
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
        for(index = 0; index < len;){
            c = text.charAt(index);
            str = new StringBuilder();
            while(c >= 48 && c <= 57){
                str.append(c);
                index++;
                if(index >= len) break;
                c = text.charAt(index);
            }
            items.add(new Pair<>(String.valueOf(str),"num"));
            items.add(new Pair<>(String.valueOf(c),"op"));
            index++;
        }
        return Calculator.calculate(items);
    }

    public static String Check(String text){
        return Calculator.getAns(text);
    }
}
