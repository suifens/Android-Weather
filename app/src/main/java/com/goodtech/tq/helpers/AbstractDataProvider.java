package com.goodtech.tq.helpers;

import com.goodtech.tq.models.CityMode;

/**
 * com.goodtech.tq.helpers
 */
public abstract class AbstractDataProvider {

    public abstract int getCount();

    public abstract CityMode getItem(int index);

    public abstract void removeItem(int position);

    public abstract void moveItem(int fromPosition, int toPosition);

    public abstract void swapItem(int fromPosition, int toPosition);

    public abstract int undoLastRemoval();

}
