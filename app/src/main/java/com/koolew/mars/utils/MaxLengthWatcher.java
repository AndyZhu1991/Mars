package com.koolew.mars.utils;

import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import com.koolew.mars.R;

/**
 * Created by jinchangzhu on 5/27/15.
 */
/*
 * 监听输入内容是否超出最大长度，并设置光标位置
 * */
public class MaxLengthWatcher implements TextWatcher {

    protected int maxLen = 0;
    protected EditText editText = null;


    public MaxLengthWatcher(int maxLen, EditText editText) {
        this.maxLen = maxLen;
        this.editText = editText;
    }

    public void afterTextChanged(Editable arg0) {
        // TODO Auto-generated method stub

    }

    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {
        // TODO Auto-generated method stub

    }

    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        // TODO Auto-generated method stub
        Editable editable = editText.getText();
        int len = editable.length();

        if(len > maxLen) {
            int selEndIndex = Selection.getSelectionEnd(editable);
            String str = editable.toString();
            //截取新字符串
            String newStr = str.substring(0,maxLen);
            editText.setText(newStr);
            editable = editText.getText();

            //新字符串的长度
            int newLen = editable.length();
            //旧光标位置超过字符串长度
            if(selEndIndex > newLen) {
                selEndIndex = editable.length();
            }
            //设置新光标所在的位置
            Selection.setSelection(editable, selEndIndex);
            onTextOverInput();
        }
    }

    // Override it if you want do something when text over inputted
    public void onTextOverInput() {
        Toast.makeText(editText.getContext(),
                editText.getContext().getString(R.string.nickname_over_input_message,
                maxLen), Toast.LENGTH_SHORT).show();
    }
}
