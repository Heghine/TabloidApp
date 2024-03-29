package com.science.inc.tabloid.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public class CustomAutoCompleteView extends AutoCompleteTextView {
	
	public CustomAutoCompleteView(Context context) {
        super(context);
    }
     
    public CustomAutoCompleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
 
    public CustomAutoCompleteView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
 
    // this is how to disable AutoCompleteTextView filter
    @Override
    protected void performFiltering(final CharSequence text, final int keyCode) {
        super.performFiltering("", keyCode);
    }
}
