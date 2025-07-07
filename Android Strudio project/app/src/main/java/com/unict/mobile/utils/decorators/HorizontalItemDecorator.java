package com.unict.mobile.utils.decorators;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HorizontalItemDecorator extends RecyclerView.ItemDecoration {
    private final int horizontalSpaceWidth;

    public HorizontalItemDecorator(int horizontalSpaceWidth) {
        this.horizontalSpaceWidth = horizontalSpaceWidth;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        if(parent.getAdapter() != null && parent.getChildAdapterPosition(view) != parent.getAdapter().getItemCount() - 1){
            float density = view.getResources().getDisplayMetrics().density;
            outRect.right = (int) (horizontalSpaceWidth * density);
        }
    }
}
