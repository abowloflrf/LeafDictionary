package cn.studyjams.s2.sj0191.ruofeng;

import java.util.List;

/**
 * Created by ruofeng on 2017/5/31.
 */

public class TranslateResult {
    private String errorCode;
    private String query;
    private List<String> translation;
    private details basic;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getTranslation() {
        return translation;
    }

    public void setTranslation(List<String> translation) {
        this.translation = translation;
    }

    public details getBasic() {
        return basic;
    }

    public void setBasic(details basic) {
        this.basic = basic;
    }

    public class details {
        private String phonetic;
        private List<String> explains;

        public String getPhonetic() {
            return phonetic;
        }

        public void setPhonetic(String phonetic) {
            this.phonetic = phonetic;
        }

        public List<String> getExplains() {
            return explains;
        }

        public void setExplains(List<String> explains) {
            this.explains = explains;
        }

    }
}
