package top.wuhaojie.bthelper;

/**
 * Created by wuhaojie on 2016/10/19 17:53.
 */

public interface Filter {

    /**
     * Check if a given data should be published.
     * @param s
     * @return
     */
    boolean isCorrect(String s);

}
