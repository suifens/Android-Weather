package com.goodtech.tq.fragement;

import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.goodtech.tq.R;

/**
 * A fragment representing a list of Items.
 */
public class WeatherFragment2 extends BaseFragment {

    protected RecyclerView mRecyclerView;

    @Override
    protected int getViewLayoutRes() {
        return R.layout.fragment_item_list;
    }

    @Override
    protected void setupCacheViews() {
        super.setupCacheViews();
        mCacheView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.color_current_location));
        mRecyclerView = (RecyclerView) mCacheView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    //    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_item_list, container, false);
//        Context context = view.getContext();
//        RecyclerView recyclerView = (RecyclerView) view;
//        recyclerView.setLayoutManager(new LinearLayoutManager(context));
////            recyclerView.setAdapter(new WeatherRecyclerAdapter(DummyContent.ITEMS, mListener));
//        return view;
//    }
}
