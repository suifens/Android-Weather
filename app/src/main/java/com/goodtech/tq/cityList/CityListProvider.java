package com.goodtech.tq.cityList;

import com.goodtech.tq.helpers.AbstractDataProvider;
import com.goodtech.tq.helpers.LocationSpHelper;
import com.goodtech.tq.models.CityMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * com.goodtech.tq.cityList
 */
public class CityListProvider extends AbstractDataProvider {

    private ArrayList<CityMode> mData;
    private CityMode mLastRemovedData;
    private int mLastRemovedPosition = -1;

    public CityListProvider() {

        mData = LocationSpHelper.getCityListAndLocation();

    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public CityMode getItem(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + index);
        }

        return mData.get(index);
    }

    @Override
    public void removeItem(int position) {
        final CityMode removedItem = mData.remove(position);
        mLastRemovedData = removedItem;
        mLastRemovedPosition = position;
    }

    @Override
    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        final CityMode item = mData.remove(fromPosition);

        mData.add(toPosition, item);
        mLastRemovedPosition = -1;
    }

    @Override
    public void swapItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        Collections.swap(mData, toPosition, fromPosition);
        mLastRemovedPosition = -1;
    }

    @Override
    public int undoLastRemoval() {
        if (mLastRemovedData != null) {
            int insertedPosition;
            if (mLastRemovedPosition >= 0 && mLastRemovedPosition < mData.size()) {
                insertedPosition = mLastRemovedPosition;
            } else {
                insertedPosition = mData.size();
            }

            mData.add(insertedPosition, mLastRemovedData);

            mLastRemovedData = null;
            mLastRemovedPosition = -1;

            return insertedPosition;
        } else {
            return -1;
        }
    }


}
