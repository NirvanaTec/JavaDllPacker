package space.pass;

import com.alibaba.fastjson2.JSONObject;

public class PassInfo{

    public String name; // 解密过程_456
    public String text; // 完整内容

    public PassInfo() {
        this("");
    }

    public PassInfo(String name, String text) {
        this.name = name;
        this.text = text;
    }

    public PassInfo(Object text) {
        this("", text.toString());
    }

    public void setText(String name, Object text) {
        this.text = this.text.replace("{$" + name + "$}", text.toString());
    }

    public void addText(String text) {
        this.text += text;
    }

    public void addText(PassInfo text) {
        this.addText(text.text);
    }

}