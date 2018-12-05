package cn.itcast.core.entity;

import java.io.Serializable;

/**
 * 封装添加返回的结果集
 */

public class Result implements Serializable {
    private boolean flag;
    private String message;

    public Result(boolean flag, String message) {
        this.flag = flag;
        this.message = message;
    }

    public boolean isFlag() {

        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
