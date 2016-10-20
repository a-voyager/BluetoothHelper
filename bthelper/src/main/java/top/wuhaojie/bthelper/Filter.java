package top.wuhaojie.bthelper;

/**
 * A Filter can be used to check if a a given response is an expect data.
 * Created by wuhaojie on 2016/10/19 17:53.
 */

public interface Filter {

    /**
     * Check if a given response should be published.
     * @param response a given response
     * @return if the response should be published
     */
    boolean isCorrect(String response);

}
