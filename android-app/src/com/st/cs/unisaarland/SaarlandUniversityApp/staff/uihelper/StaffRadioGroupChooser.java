package com.st.cs.unisaarland.SaarlandUniversityApp.staff.uihelper;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RadioGroup;
import com.st.cs.unisaarland.SaarlandUniversityApp.R;

/**
 * Created with IntelliJ IDEA.
 * User: Shahzad
 * Date: 12/3/13
 * Time: 3:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class StaffRadioGroupChooser extends RadioGroup {
    public StaffRadioGroupChooser(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        changeButtonsImages();
    }

    private void changeButtonsImages() {
        super.getChildAt(0).setBackgroundResource(R.drawable.segment_button);
        super.getChildAt(1).setBackgroundResource(R.drawable.segment_button);
    }
}
