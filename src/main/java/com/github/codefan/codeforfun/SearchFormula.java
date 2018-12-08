package com.github.codefan.codeforfun;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.math3.fraction.Fraction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SearchFormula {
    public static Map<String, List<String>> foundReslutions = new HashMap<>(100);

    //计算逆波兰式
    public static Fraction calcReversePolishRepresentation(Object[] reversePolish) {
        int len = reversePolish.length;
        Fraction[] stack = new Fraction[len/2 + 1];
        int j = 0;
        for (int i = 0; i < len; i++) {
            if (reversePolish[i] instanceof Integer) {
                stack[j] =  new Fraction((Integer) reversePolish[i]);
                j++;
            } else {
                switch ((String) reversePolish[i]) {
                    case "+":
                        stack[j - 2] = stack[j - 2].add(stack[j - 1]);
                        break;
                    case "-":
                        stack[j - 2] = stack[j - 2].subtract(stack[j - 1]);
                        break;
                    case "*":
                        stack[j - 2] = stack[j - 2].multiply(stack[j - 1]);
                        break;
                    case "/":
                        if (stack[j - 1].equals(Fraction.ZERO)) {
                            return Fraction.MINUS_ONE;
                        }
                        stack[j - 2] = stack[j - 2].divide(stack[j - 1]);
                        break;
                }
                j--;
            }
        }
        return stack[0];
    }

    // 根据 二元运算符号的数量（nOps） 穷举所有的情况，数量为 nOps的4次方
    // 这个比较大 没有采用返回 数组的方式，用消费者模式
    public static void makeOperatorArray(int nOps , Consumer<String[]> consumer ){
        String[] opts = {"+","-","*","/","A"};
        String[] opArr = new String[nOps];
        int [] nPos = new int[nOps];
        for(int i=0; i<nOps; i++){
            opArr[i] = opts[0];
            nPos[i] = 0;
        }
        int j = 0;
        while(true){
            consumer.accept(opArr);
            //System.out.println( StringUtils.join(opArr,","));
            nPos[j] = nPos[j] + 1;

            while(j<nOps && nPos[j]==4){
                nPos[j] = 0;
                opArr[j] = opts[0];
                j++;
                if(j>=nOps){
                    break;
                }
                nPos[j] = nPos[j] + 1;
            }

            if(j>=nOps){
                break;
            }else{
                opArr[j] = opts[nPos[j]];
                j = 0;
            }
        }
    }

    // 根据给出的 数据 和 运算符号，来穷举 不同的 组合方式
    // 对于 逆波兰式 来说 就是变换 运算符号的位置
    // 并计算结果
    // 对结果等于 filterValue 的表达式 执行 consumer 消费方法
    public static void makeFormulaAndCalc(List<Integer> data, String[] opts,
                                          int filterValue , Consumer<Object[]> consumer ){
        int dN = data.size();
        int oN = opts.length;
        assert dN == oN +1;
        Object[] reversePolish = new Object[dN + oN];
        reversePolish[0] = data.get(0);
        reversePolish[1] = data.get(1);
        int [] optPos =  new int [oN];
        for(int i=0; i< oN; i++ ){
            optPos[i] = 1;
        }

        while (true){
            //System.out.println( StringUtils.join(optPos,','));
            int dPos = 2;
            int oPos = 0;
            for(int i=0; i<oN ; i++){
                for(int j=0; j<optPos[i]; j++){
                    reversePolish[dPos] = opts[oPos];
                    dPos ++;
                    oPos ++;
                }
                if(i<oN-1) {
                    reversePolish[dPos] = data.get(2 + i);
                }
                dPos++;
            }

            Fraction f = calcReversePolishRepresentation(reversePolish);
            if(f.equals(new Fraction(filterValue))){
                consumer.accept(reversePolish);
                //System.out.println( StringUtils.join(reversePolish,','));
            }
            int i=0;
            while(i<oN-1){
                if(optPos[i] == 0 ){
                    i++;
                }else{
                    optPos[i] = optPos[i]-1;
                    optPos[i+1] = optPos[i+1]+1;
                    int j = 0;
                    while(optPos[i] > 0){
                        optPos[j] = 1;
                        j++;
                        optPos[i] = optPos[i] -1;
                    }
                    break;
                }
            }
            if(i>=oN-1){
                break;
            }
        }

    }

    // 对给定的 数据 通过组合运算符号的方式 来 筛选符合 filterValue 结果的方式
    public static void searchFormulaAndCalc(List<Integer> data, int filterValue, Consumer<Object[]> consumer){
        int nD = data.size();
        makeOperatorArray(nD-1, (opts) -> makeFormulaAndCalc(data, opts, filterValue , consumer));
    }

    // 算24点 并将结果的逆波兰式转换为 四则运算表达式
    @SuppressWarnings("unchecked")
    public static void transPolish(Object[] reversePolish){
        int len = reversePolish.length;
        Pair<String, String>[] stack = new Pair[len/2 +1];
        int j = 0;
        for (int i = 0; i < len; i++) {
            if (reversePolish[i] instanceof Integer) {
                stack[j] = new ImmutablePair<>("O",String.valueOf(reversePolish[i]));
                j++;
            } else {
                switch ((String) reversePolish[i]) {
                    case "+":
                        stack[j - 2] =
                                new ImmutablePair<>("+",
                                        stack[j - 2].getRight()+"+"+stack[j - 1].getRight()
                                );
                        break;
                    case "-":
                        stack[j - 2] =
                                new ImmutablePair<>("-",
                                        stack[j - 2].getRight()+
                                                ( StringUtils.equalsAny( stack[j - 1].getLeft(), "-","+")
                                                        ? "-("+stack[j - 1].getRight()+")"
                                                        : "-"+stack[j - 1].getRight())
                                );
                        break;
                    case "*":
                        stack[j - 2] =
                                stack[j - 2] =
                                        new ImmutablePair<>("*",
                                                ( StringUtils.equalsAny( stack[j - 2].getLeft(), "-","+")
                                                        ? "("+stack[j - 2].getRight()+")"
                                                        : stack[j - 2].getRight())
                                                        +
                                                        ( StringUtils.equalsAny( stack[j - 1].getLeft(), "-","+")
                                                                ? "*("+stack[j - 1].getRight()+")"
                                                                : "*"+stack[j - 1].getRight())
                                        );
                        break;
                    case "/":
                        stack[j - 2] =
                                new ImmutablePair<>("/",
                                        ( StringUtils.equalsAny( stack[j - 2].getLeft(), "-","+")
                                                ? "("+stack[j - 2].getRight()+")"
                                                : stack[j - 2].getRight())
                                                +
                                                ( StringUtils.equalsAny( stack[j - 1].getLeft(), "-","+","*","/")
                                                        ? "/("+stack[j - 1].getRight()+")"
                                                        : "/"+stack[j - 1].getRight())
                                );
                        break;
                }
                j--;
            }
        }
        String rb = StringUtils.join(reversePolish, " ");
        List<String> rbs = foundReslutions.get(stack[0].getRight());
        if(rbs==null){
            rbs = new ArrayList<>();
            rbs.add(rb);
            foundReslutions.put(stack[0].getRight(), rbs);
        }else{
            rbs.add(rb);
        }
    }

    //判断输入的是否为数值
    public static boolean isNumber(String strNum) {
        if(StringUtils.isBlank(strNum)){
            return false;
        }
        for(int i=0; i<strNum.length(); i++){
            if(strNum.charAt(i)<'0' || strNum.charAt(i)>'9'){
                return false;
            }
        }
        return true;
    }

    public static void showResult(){
        //展示结果
        int sc=0;
        for(Map.Entry<String,List<String>> ent : foundReslutions.entrySet()){
            sc ++;
            System.out.print((sc<10?" "+sc+ ": ": sc + ": ")+ ent.getKey());
            for(int i= ent.getKey().length(); i<16; i++ ){
                System.out.print(" ");
            }
            System.out.println(ent.getValue().get(0));
            for(int i=1; i<ent.getValue().size();i++){
                System.out.print("                    ");
                System.out.println(ent.getValue().get(i));
            }
        }
        System.out.println("一共中找到 " + sc + " 个不同方案。");
        foundReslutions.clear();
    }


    public static void main(String arg[]) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            System.out.println("请在一行中输入用空格隔开的整数，最后一个数字为计算结果。退出请输入exit：");
            String s = br.readLine().trim();
            if(StringUtils.isBlank(s)){
                continue;
            }
            if(StringUtils.equalsIgnoreCase("exit",s)){
                break;
            }

            String[] nums = s.split(" ");
            List<Integer> alist = new ArrayList<>(nums.length+1);
            Integer nRet = null;

            for (String num : nums) {
                if (isNumber(num)) {
                    if(nRet != null) {
                        alist.add(nRet);
                    }
                    nRet = Integer.valueOf(num);
                }
            }
            if( alist.size() < 2){
                continue;
            }
            final Integer nFilter = nRet;
            searchFormulaAndCalc(alist,
                            nFilter, SearchFormula::transPolish);
                            //(reversePolish)-> System.out.println( StringUtils.join(reversePolish,','))));
            showResult();
        }
    }
}
