package top.wuhaojie.bthelper;

/**
 * Listener for send message process.
 * Created by wuhaojie on 2016/9/10 20:17.
 */
public interface OnSendMessageListener extends IErrorListener, IConnectionLostListener {
    /**
     * Call when send a message succeed, and get a response from the remote device.
     *
     * @param status   the status describes ok or error.
     *                 1 respect the response is valid,
     *                 -1 respect the response is invalid
     * @param response the response from the remote device
     */
    void onSuccess(int status, String response);
}
