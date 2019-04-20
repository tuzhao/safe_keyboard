package org.tuzhao.keyboard.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.Keyboard.Key;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.PopupWindow;

import com.tuzhao.keyboard.R;

import org.tuzhao.keyboard.utils.DisplayUtils;
import org.tuzhao.keyboard.wiget.BaseOnClickListener;
import org.tuzhao.keyboard.wiget.SecurityEditText;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 安全键盘主类
 *
 * @author tuzhao
 */
public final class SafeKeyboard extends PopupWindow {

    private static final String TAG = "SafeKeyboard";

    private KeyboardView keyboardView;

    /**
     * 字母键盘
     */
    private Keyboard mKeyboardLetter;

    /**
     * 符号键盘
     */
    private Keyboard mKeyboardSymbol;

    /**
     * 数字键盘
     */
    private Keyboard mKeyboardNumber;

    /**
     * 是否大写
     */
    private boolean isUpper = false;

    private ArrayList<String> nums_ = new ArrayList<>();

    private SecurityEditText curEditText;
    private Context mContext;
    private View mPositionView;
    private static boolean debug = false;

    @SuppressLint("ClickableViewAccessibility")
    public SafeKeyboard(Context context, SecurityEditText et) {
        super(context);
        mContext = context;
        mPositionView = et;
        curEditText = et;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams")
        View root = inflater.inflate(R.layout.skb_layout_keyboard, null);
        this.setContentView(root);
        this.setWidth(DisplayUtils.getScreenWidth(mContext));
        this.setHeight(LayoutParams.WRAP_CONTENT);
        ColorDrawable dw = new ColorDrawable(Color.parseColor("#00000000"));
        this.setBackgroundDrawable(dw);

        this.setFocusable(true);
        this.setOutsideTouchable(true);
        this.setPopupWindowTouchModal(this, false);

        this.setAnimationStyle(R.style.PopupKeybroad);
        mKeyboardLetter = new Keyboard(mContext, R.xml.skb_keyboard_english);
        mKeyboardNumber = new Keyboard(mContext, R.xml.skb_keyboard_number);
        mKeyboardSymbol = new Keyboard(mContext, R.xml.skb_keyboard_symbols);

        keyboardView = root.findViewById(R.id.keyboard_view);
        View mCloseIv = root.findViewById(R.id.skb_pop_close_iv);
        mCloseIv.setOnClickListener(new CloseClickListener());

        initNumbers();
        randomNumbers();
        keyboardView.setKeyboard(mKeyboardNumber);
        keyboardView.setEnabled(true);
        keyboardView.setPreviewEnabled(false);
        keyboardView.setOnKeyboardActionListener(new SimpleKeyboardActionListener());

        et.setOnTouchListener(new DealTouchListener());
        et.setOnFocusChangeListener(new FocusChangeListener());
    }

    private final class FocusChangeListener implements View.OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            log("onFocusChange() hasFocus: " + hasFocus);
            if (!hasFocus) {
                hideKeyboard();
            }
        }
    }

    private final class DealTouchListener implements View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            log("onTouch(): " + event);
            if (event.getAction() == MotionEvent.ACTION_UP) {
                curEditText.requestFocus();
                hideSystemKeyboard(curEditText);
                Editable editable = curEditText.getText();
                if (null != editable) {
                    Selection.setSelection(editable, editable.length());
                }
                showKeyboard(mPositionView);
                return true;
            }
            return false;
        }
    }

    private final class CloseClickListener extends BaseOnClickListener {
        @Override
        public void click(View v) {
            hideKeyboard();
        }
    }

    /**
     * @param popupWindow popupWindow 的touch事件传递
     * @param touchModal  true代表拦截，事件不向下一层传递，false表示不拦截，事件向下一层传递
     */
    @SuppressLint("PrivateApi")
    private void setPopupWindowTouchModal(PopupWindow popupWindow, boolean touchModal) {
        Method method;
        try {
            method = PopupWindow.class.getDeclaredMethod("setTouchModal", boolean.class);
            method.setAccessible(true);
            method.invoke(popupWindow, touchModal);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void hideSystemKeyboard(View view) {
        InputMethodManager manager = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager != null) {
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void initNumbers() {
        nums_.clear();
        nums_.add("48#0");
        nums_.add("49#1");
        nums_.add("50#2");
        nums_.add("51#3");
        nums_.add("52#4");
        nums_.add("53#5");
        nums_.add("54#6");
        nums_.add("55#7");
        nums_.add("56#8");
        nums_.add("57#9");
    }

    private final class SimpleKeyboardActionListener implements OnKeyboardActionListener {

        @Override
        public void onPress(int primaryCode) {

        }

        @Override
        public void onRelease(int primaryCode) {

        }

        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            log("onKey() primaryCode: " + primaryCode + " keyCodes: " + Arrays.toString(keyCodes));
            Keyboard temp = keyboardView.getKeyboard();
            Editable editable = curEditText.getText();
            int start = curEditText.getSelectionStart();
            if (primaryCode == Keyboard.KEYCODE_CANCEL) {
                // 完成按钮所做的动作
                hideKeyboard();
            } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
                // 删除按钮所做的动作
                if (editable != null && editable.length() > 0) {
                    if (start > 0) {
                        editable.delete(start - 1, start);
                    }
                }
            } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
                // 大小写切换
                changeKey();
                keyboardView.setKeyboard(mKeyboardLetter);

            } else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE) {
                if (temp == mKeyboardNumber) {
                    keyboardView.setKeyboard(mKeyboardLetter);
                }
                if (temp == mKeyboardLetter) {
                    keyboardView.setKeyboard(mKeyboardNumber);
                }
                if (temp == mKeyboardSymbol) {
                    keyboardView.setKeyboard(mKeyboardNumber);
                }
            } else if (primaryCode == 57419) {
                //左移
                if (start > 0) {
                    curEditText.setSelection(start - 1);
                }
            } else if (primaryCode == 57421) {
                //右移
                if (start < curEditText.length()) {
                    curEditText.setSelection(start + 1);
                }
            } else if (primaryCode == 100860) {
                if (temp == mKeyboardLetter) {
                    keyboardView.setKeyboard(mKeyboardSymbol);
                }
                if (temp == mKeyboardSymbol) {
                    keyboardView.setKeyboard(mKeyboardLetter);
                }
            } else {
                if (null != editable) {
                    editable.insert(start, Character.toString((char) primaryCode));
                }
            }
        }

        @Override
        public void onText(CharSequence text) {

        }

        @Override
        public void swipeLeft() {

        }

        @Override
        public void swipeRight() {

        }

        @Override
        public void swipeDown() {

        }

        @Override
        public void swipeUp() {

        }
    }

    /**
     * 键盘大小写切换
     */
    private void changeKey() {
        List<Key> keylist = mKeyboardLetter.getKeys();
        if (isUpper) {
            isUpper = false;
            for (Key key : keylist) {
                if (key.label != null && isLetter(key.label.toString())) {
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] = key.codes[0] + 32;
                }
                if (key.codes[0] == -1) {
                    key.icon = mContext.getResources().getDrawable(
                            R.drawable.skb_keyboard_shift);
                }
            }
        } else {// 小写切换大写
            isUpper = true;
            for (Key key : keylist) {
                if (key.label != null && isLetter(key.label.toString())) {
                    key.label = key.label.toString().toUpperCase();
                    key.codes[0] = key.codes[0] - 32;
                }
                if (key.codes[0] == -1) {
                    key.icon = mContext.getResources().getDrawable(
                            R.drawable.skb_keyboard_shift_c);
                }
            }
        }
    }

    /**
     * 键盘数字随机切换
     */
    private void randomNumbers() {
        List<Key> keylist = mKeyboardNumber.getKeys();
        ArrayList<String> temNum = new ArrayList<>(nums_);

        for (Key key : keylist) {
            if (key.label != null && isNumber(key.label.toString())) {
                int number = new Random().nextInt(temNum.size());
                String[] text = temNum.get(number).split("#");
                key.label = text[1];
                key.codes[0] = Integer.valueOf(text[0], 10);
                temNum.remove(number);
            }
        }
    }

    /**
     * 弹出键盘
     *
     * @param view View
     */
    @SuppressLint("RtlHardcoded")
    private void showKeyboard(View view) {
        Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.skb_push_bottom_in);
        showAtLocation(view, Gravity.BOTTOM | Gravity.LEFT, 0, 0);
        getContentView().setAnimation(animation);
    }

    /**
     * 隐藏键盘
     */
    private void hideKeyboard() {
        this.dismiss();
    }

    private boolean isLetter(String str) {
        String letterStr = mContext.getString(R.string.aToz);
        return letterStr.contains(str.toLowerCase());
    }

    private boolean isNumber(String str) {
        String numStr = mContext.getString(R.string.zeroTonine);
        return numStr.contains(str.toLowerCase());
    }

    public void setDebug(boolean debug) {
        SafeKeyboard.debug = debug;
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    private static void log(String msg) {
        if (debug) {
            Log.d(TAG, msg);
        }
    }

}
