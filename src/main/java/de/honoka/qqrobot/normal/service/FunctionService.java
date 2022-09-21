package de.honoka.qqrobot.normal.service;

import de.honoka.qqrobot.normal.util.EncodingUtils;
import de.honoka.qqrobot.normal.util.HtmlUtils;
import lombok.SneakyThrows;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 无法归类的功能逻辑，放入功能服务类中
 */
@Service
public class FunctionService {

    /**
     * 获取百度百科词条简介
     *
     * @return 内容
     */
    @SneakyThrows
    public String getBaike(String[] args) {
        StringBuilder keyword = new StringBuilder();
        for(String arg : args) {
            keyword.append(arg).append(" ");
        }
        keyword = new StringBuilder(EncodingUtils.encodeURIComponent(
                keyword.toString().trim()));
        Document html = Jsoup.connect("https://baike.baidu.com/item/" +
                keyword).get();
        String text;    //将被反复处理的简介信息
        try {
            Element ele = html.body();
            //获取简介内容所在标签，提取过程中可能会出现NullPointer
            Elements content = ele.select(".lemma-summary");
            //清除简介标签内容中的html标签，解码html转义字符
            text = HtmlUtils.clearHtmlTags(content.first().outerHtml());
        } catch(NullPointerException npe) {
            return "没有找到相应的词条";
        }
        text = text.replace("\n", "");  //清除多余的换行
        //文本中可能存在由160号空格即nbsp转化来的空格，需要先将其替换为普通空格
        text = text.replace("\u00A0", " ");
        //清除百科文本内的方括号体和周围的空格
        char[] chars = text.toCharArray();
        //括号体类，记录需要被清除的括号体的左右括号位置和具体值
        class BracketsBody {

            int left, right;

            String value;
        }
        List<BracketsBody> list = new ArrayList<>();
        //查找内容为纯数字（可能有减号）或为空的括号体的位置
        for(int i = 0; i < chars.length; i++) {
            if(chars[i] == '[') {
                BracketsBody b = new BracketsBody();
                //记录下左括号位置，从下一位开始继续检查，直到遇到右括号或非数字
                b.left = i;
                //检测到左括号后才检测右括号
                for(i++; i < chars.length; i++) {
                    //只检查括号中全是数字（或带减号）或为空的括号体
                    if(chars[i] == ']') {
                        b.right = i;
                        list.add(b);
                        break;
                    }
                    //遇到非数字非减号，这一组括号不记录，重新从左括号开始检查
                    if((chars[i] < '0' || chars[i] > '9') &&
                       chars[i] != '-') {
                        break;
                    }
                }
            }
        }
        //分割出要清除的括号体内容
        for(BracketsBody b : list) {
            b.value = text.substring(b.left, b.right + 1);
        }
        //将截取范围扩展到每个括号体左右两侧的空格上
        //连带选取括号体左右两侧的空格，清除括号体
        for(BracketsBody b : list) {
            int left = text.indexOf(b.value);
            int right = left + b.value.length() - 1;
            //需要加入边界判断，防止非法访问边界外字符
            while(left > 0 && text.charAt(left - 1) == ' ') {
                left--;
            }
            while(right < text.length() - 1 &&
                  text.charAt(right + 1) == ' ') {
                right++;
            }
            b.value = text.substring(left, right + 1);
            text = text.substring(0, left) + text.substring(
                    right + 1);
            //此方法不可使用，必须严格按照列表顺序一个个替换
            //str = str.replace(b.value, "");
        }
        return text;
    }
}
