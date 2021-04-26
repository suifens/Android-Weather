package com.goodtech.tq.views;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

public class SwitchButton extends View {
    private static final int DEFAULT_WIDTH = dp2pxInt(58);
    private static final int DEFAULT_HEIGHT = dp2pxInt(36);
    
    /**
     * 动画状态：
     * 0.静止
     * 1.进入拖动
     * 2.处于拖动
     * 3.拖动-复位
     * 4.拖动-切换
     * 5.点击切换
     **/
    private final int ANIMATE_STATE_NONE = 0;
    private final int ANIMATE_STATE_PENDING_DRAG = 1;
    private final int ANIMATE_STATE_DRAGING = 2;
    private final int ANIMATE_STATE_PENDING_RESET = 3;
    private final int ANIMATE_STATE_PENDING_SETTLE = 4;
    private final int ANIMATE_STATE_SWITCH = 5;

    /**
     * 背景半径
     */
    private float viewRadius;
    
    /**
     * 背景高
     */
    private float height;
    /**
     * 背景宽
     */
    private float width;
    /**
     * 背景位置
     */
    private float left;
    private float top;
    private float right;
    private float bottom;
    
    /**
     * 背景底色
     */
    private int background;
    /**
     * 背景关闭颜色
     */
    private int uncheckColor;
    /**
     * 背景打开颜色
     */
    private int checkedColor;
    /**
     * 边框宽度px
     */
    private int borderWidth;
    
    
    protected float mButtonWidth;
    /**
     * 按钮最左边
     */
    private float buttonMinX;
    /**
     * 按钮最右边
     */
    private float buttonMaxX;
    
    /**
     * 背景画笔
     */
    private Paint paint;
    
    
    /**
     * 当前状态
     */
    private ViewState viewState;
    private ViewState beforeState;
    private ViewState afterState;
    
    private RectF rect = new RectF();
    /**
     * 动画状态
     */
    private int animateState = ANIMATE_STATE_NONE;
    
    /**
     *
     */
    private ValueAnimator valueAnimator;
    
    private final android.animation.ArgbEvaluator argbEvaluator = new android.animation.ArgbEvaluator();
    
    /**
     * 是否选中
     */
    private boolean isSelectRight;
    /**
     * 是否启用动画
     */
    private boolean enableEffect;
    
    /**
     * 收拾是否按下
     */
    private boolean isTouchingDown = false;
    /**
     *
     */
    private boolean isUiInited = false;
    /**
     *
     */
    private boolean isEventBroadcast = false;
    
    private OnCheckedChangeListener onCheckedChangeListener;
    
    /**
     * 手势按下的时刻
     */
    private long touchDownTime;
    
    
    protected Paint mTextPaint;
    Rect bounds = new Rect();

    protected float mLineWidth;

    protected int mSelectedIndex = 0;
    protected String[] mTitles = new String[2];
    
    public SwitchButton(Context context) {
        super(context);
        init(context, null);
    }
    
    public SwitchButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }
    
    public SwitchButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void setButtonTitles(int[] titleRes) {
        mTitles = new String[titleRes.length];
        for (int i = 0; i < titleRes.length; i++) {
            mTitles[i] = getContext().getString(titleRes[i]);
        }
        postInvalidate();
    }

    public void setButtonTitles(String[] titles) {
        mTitles = titles;
        postInvalidate();
    }
    
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SwitchButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }
    
    @Override
    public final void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(0, 0, 0, 0);
    }
    
    /**
     * 初始化参数
     */
    private void init(Context context, AttributeSet attrs) {
        
//        TypedArray typedArray = null;
//        if (attrs != null) {
//            typedArray = context.obtainStyledAttributes(attrs, R.styleable.SwitchButton);
//        }
        uncheckColor = 0X80ffffff;//0XffDDDDDD;
        checkedColor =  0XFFFFAD00; //0Xff51d367;
        borderWidth = dp2pxInt(1);//dp2pxInt(1);
        isSelectRight = false;
        background = 0XFFFFAD00;//Color.WHITE;
        enableEffect = true;
        long effectDuration = 200;
//
//        if (typedArray != null) {
//            typedArray.recycle();
//        }



        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        viewState = new ViewState();
        beforeState = new ViewState();
        afterState = new ViewState();
        
        valueAnimator = ValueAnimator.ofFloat(0f, 1f);
        valueAnimator.setDuration(effectDuration);
        valueAnimator.setRepeatCount(0);
        valueAnimator.addUpdateListener(animatorUpdateListener);
        valueAnimator.addListener(animatorListener);
        
        super.setClickable(true);
        this.setPadding(0, 0, 0, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        }


        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);// 设置画笔的锯齿效果
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextSize(dp2pxInt(16));
    }

    public void showAlternateText(int spSize) {
        AssetManager assetManager = getContext().getAssets();
        Typeface fontType = Typeface.createFromAsset(assetManager, "fonts/DINAlternate-Bold.ttf");
        mTextPaint.setTypeface(fontType);
        mTextPaint.setTextSize(dp2pxInt(spSize));
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        
        if (widthMode == MeasureSpec.UNSPECIFIED
                || widthMode == MeasureSpec.AT_MOST) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(DEFAULT_WIDTH, MeasureSpec.EXACTLY);
        }
        if (heightMode == MeasureSpec.UNSPECIFIED
                || heightMode == MeasureSpec.AT_MOST) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(DEFAULT_HEIGHT, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    protected float mOneDipPx;
    
    protected float mSelectF = 1.08f;
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        
        float viewPadding = borderWidth;
        
        height = h - viewPadding - viewPadding;
        width = w - viewPadding - viewPadding;
        
        viewRadius = height * .5f;
        
        left = viewPadding;
        top = viewPadding;
        right = w - viewPadding;
        bottom = h - viewPadding;
        
        
        mButtonWidth = width / mTitles.length;
        buttonMinX = left;
        buttonMaxX = (float) (right - mButtonWidth*mSelectF);
        
        isUiInited = true;
        
        mOneDipPx = dp2pxInt(2f);

        if (mSelectedIndex != 0 && viewState != null) {
            setSelectViewState(viewState, mSelectedIndex);
        }
        
        postInvalidate();
        
    }
    
    public void setUncheckColor(int color){
        this.uncheckColor = color;
        postInvalidate();
    }
    
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < mTitles.length; i++) {
            String title = mTitles[i];
            if (!TextUtils.isEmpty(title)) {
                if (mSelectedIndex == i) {
                    mTextPaint.setColor(checkedColor);
                } else {
                    mTextPaint.setColor(uncheckColor);
                }
                mTextPaint.setTextAlign(Paint.Align.CENTER);
                mTextPaint.getTextBounds(title, 0, title.length(), bounds);
                canvas.drawText(title, (((int) (mButtonWidth*mSelectF) >> 1) + (mButtonWidth * i)), (((int) (top + height)) >> 1) + (bounds.height() >> 1),
                        mTextPaint );

                if (mSelectedIndex == i) {
                    mLineWidth = Math.min(mButtonWidth, bounds.width());
                }
            }
        }

        //绘制按钮
        drawButton(canvas, viewState.buttonStartX);
    }
    
    
    /**
     * @param canvas
     * @param left
     * @param top
     * @param right
     * @param bottom
     * @param backgroundRadius
     * @param paint
     */
    private void drawRoundRect(Canvas canvas, float left, float top, float right, float bottom, float backgroundRadius, Paint paint) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(left, top, right, bottom, backgroundRadius, backgroundRadius, paint);
        } else {
            rect.set(left, top, right, bottom);
            canvas.drawRoundRect(rect, backgroundRadius, backgroundRadius, paint);
        }
    }

    private void drawLine(Canvas canvas, float left, float top, float right, float bottom, Paint paint) {
        canvas.drawLine(left, top, right, bottom, paint);
    }
    
    /**
     * @param canvas
     * @param startX px
     */
    private void drawButton(Canvas canvas, float startX) {
        
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(mOneDipPx);
        paint.setColor(background);
//        drawRoundRect(canvas, startX, top, (float) (startX + mButtonWidth*mSelectF), top + height, viewRadius, paint);
        float centerX = (float) (startX + mButtonWidth * mSelectF / 2);
        drawLine(canvas, (centerX - mLineWidth), top + height - 10, (centerX + mLineWidth), top + height - 10, paint);
    }
    
    
    //    @Override
    public boolean isSelectRight() {
        return isSelectRight;
    }

    public void setNormalSelected(int index) {
        mSelectedIndex = index;
    }

    private void setSelectViewState(ViewState viewState, int index) {
        mSelectedIndex = index;
        beforeState.copy(viewState);
        afterState.copy(viewState);

        float startX = left + (mButtonWidth * index);
        viewState.buttonStartX = startX;
        afterState.buttonStartX = startX;
    }
    
    //    @Override
    public void toggle() {
//        toggle(true);
    }
    
    /**
     * 切换状态
     *
     * @param animate
     */
    public void toggle(int type, boolean animate) {
        toggle(type, animate, true);
    }
    
    /**
     * @param toType    0切换到左边，1切换到右边
     * @param animate
     * @param broadcast
     */
    private void toggle(int toType, boolean animate, boolean broadcast) {
        if (!isEnabled()) {
            return;
        }

        if (isEventBroadcast) {
            throw new RuntimeException("should NOT switch the state in method: [onCheckedChanged]!");
        }
        if (!isUiInited) {
//            isSelectRight = !isSelectRight;
//            if (broadcast) {
//                broadcastEvent();
//            }
            return;
        }
        
        if (toType == mSelectedIndex) {
            return;
        }
        
        if (!enableEffect || !animate) {
            if (valueAnimator.isRunning()) {
                valueAnimator.cancel();
            }
            setSelectViewState(viewState, toType);
            postInvalidate();
            if (broadcast) {
                broadcastEvent();
            }
            return;
        }
        
        if (animateState == ANIMATE_STATE_SWITCH) {
            return;
        }
        
        if (valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        animateState = ANIMATE_STATE_SWITCH;
        beforeState.copy(viewState);

//        if (isSelectRight()) {
//            setSelectLeftViewState(viewState);
//        } else {
//            setSelectRightViewState(afterState);
//        }
        setSelectViewState(viewState, toType);
        valueAnimator.start();
    }
    
    /**
     *
     */
    private void broadcastEvent() {
        if (onCheckedChangeListener != null) {
            isEventBroadcast = true;
            onCheckedChangeListener.onCheckedChanged(this, mSelectedIndex);
        }
        isEventBroadcast = false;
    }
    
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }
        int actionMasked = event.getActionMasked();
        
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: {
                isTouchingDown = true;
                touchDownTime = System.currentTimeMillis();
                //取消准备进入拖动状态
                removeCallbacks(postPendingDrag);
                //预设100ms进入拖动状态
                //TODO 屏蔽拖拉
//                postDelayed(postPendingDrag, 100);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                float eventX = event.getX();
                if (isPendingDragState()) {
                    //在准备进入拖动状态过程中，可以拖动按钮位置
                    float fraction = eventX / getWidth();
                    fraction = Math.max(0f, Math.min(1f, fraction));
                    
                    viewState.buttonStartX = buttonMinX + (buttonMaxX - buttonMinX) * fraction;
                    
                } else if (isDragState()) {
                    //拖动按钮位置，同时改变对应的背景颜色
                    float fraction = eventX / getWidth();
                    fraction = Math.max(0f, Math.min(1f, fraction));
                    
                    viewState.buttonStartX = buttonMinX + (buttonMaxX - buttonMinX) * fraction;
                    
                    
                    viewState.leftTitleColor = (int) argbEvaluator.evaluate(fraction, checkedColor, uncheckColor);
                    viewState.rightTitleColor = (int) argbEvaluator.evaluate(fraction, uncheckColor, checkedColor);
                    postInvalidate();
                    
                }
                break;
            }
            case MotionEvent.ACTION_UP: {
                isTouchingDown = false;
                //取消准备进入拖动状态
                removeCallbacks(postPendingDrag);
                
                if (System.currentTimeMillis() - touchDownTime <= 300) {
                    //点击时间小于300ms，认为是点击操作
                    float eventX = event.getX();
                    if (mButtonWidth > 0) {
                        float fraction = eventX / mButtonWidth;
                        fraction = Math.max(0f, fraction);
//
//                        if (fraction > .5f) {
//                            toggle(1, true);
//                        } else {
//                            toggle(0, true);
//                        }
                        toggle((int) Math.floor(fraction), true);
                    }
                } else if (isDragState()) {
                    //在拖动状态，计算按钮位置，设置是否切换状态
                    float eventX = event.getX();
                    float fraction = eventX / getWidth();
                    fraction = Math.max(0f, Math.min(1f, fraction));
                    boolean newCheck = fraction > .5f;
                    if (newCheck == isSelectRight()) {
                        pendingCancelDragState();
                    } else {
                        isSelectRight = newCheck;
                        pendingSettleState();
                    }
                } else if (isPendingDragState()) {
                    //在准备进入拖动状态过程中，取消之，复位
                    pendingCancelDragState();
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                isTouchingDown = false;
                
                removeCallbacks(postPendingDrag);
                
                if (isPendingDragState()
                        || isDragState()) {
                    //复位
                    pendingCancelDragState();
                }
                break;
            }
        }
        return true;
    }
    
    
    /**
     * 是否在动画状态
     *
     * @return
     */
    private boolean isInAnimating() {
        return animateState != ANIMATE_STATE_NONE;
    }
    
    /**
     * 是否在进入拖动或离开拖动状态
     *
     * @return
     */
    private boolean isPendingDragState() {
        return animateState == ANIMATE_STATE_PENDING_DRAG || animateState == ANIMATE_STATE_PENDING_RESET;
    }
    
    /**
     * 是否在手指拖动状态
     *
     * @return
     */
    private boolean isDragState() {
        return animateState == ANIMATE_STATE_DRAGING;
    }
    
    
    public void setEnableEffect(boolean enable) {
        this.enableEffect = enable;
    }

    public void setSelectIndex(int index) {
        toggle(index, false, false);
    }
    
    /**
     * 开始进入拖动状态
     */
    private void pendingDragState() {
        if (isInAnimating()) {
            return;
        }
        if (!isTouchingDown) {
            return;
        }
        
        if (valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        
        animateState = ANIMATE_STATE_PENDING_DRAG;
        
        beforeState.copy(viewState);
        afterState.copy(viewState);
        
        if (isSelectRight()) {
            
            viewState.leftTitleColor = uncheckColor;
            viewState.rightTitleColor = checkedColor;
            afterState.buttonStartX = buttonMaxX;
        } else {
            viewState.leftTitleColor = checkedColor;
            viewState.rightTitleColor = uncheckColor;
            afterState.buttonStartX = buttonMinX;
        }
        
        valueAnimator.start();
    }
    
    
    /**
     * 取消拖动状态
     */
    private void pendingCancelDragState() {
        if (isDragState() || isPendingDragState()) {
            if (valueAnimator.isRunning()) {
                valueAnimator.cancel();
            }
            
            animateState = ANIMATE_STATE_PENDING_RESET;
            beforeState.copy(viewState);

            valueAnimator.start();
        }
    }
    
    
    /**
     * 动画-设置新的状态
     */
    private void pendingSettleState() {
        if (valueAnimator.isRunning()) {
            valueAnimator.cancel();
        }
        
        animateState = ANIMATE_STATE_PENDING_SETTLE;
        beforeState.copy(viewState);

        valueAnimator.start();
    }
    
    
    @Override
    public final void setOnClickListener(OnClickListener l) {
    }
    
    @Override
    public final void setOnLongClickListener(OnLongClickListener l) {
    }
    
    public void setOnCheckedChangeListener(OnCheckedChangeListener l) {
        onCheckedChangeListener = l;
    }
    
    public interface OnCheckedChangeListener {
        void onCheckedChanged(SwitchButton view, int selectedIndex);
    }
    
    /*******************************************************/
    private static float dp2px(float dp) {
        Resources r = Resources.getSystem();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }
    
    private static int dp2pxInt(float dp) {
        return (int) dp2px(dp);
    }

    private static int sp2pxInt(float dp) {
        Resources r = Resources.getSystem();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, dp, r.getDisplayMetrics());
    }
    
    private static int optInt(TypedArray typedArray, int index, int def) {
        if (typedArray == null) {
            return def;
        }
        return typedArray.getInt(index, def);
    }
    
    private static String optString(TypedArray typedArray, int index) {
        if (typedArray == null) {
            return null;
        }
        return typedArray.getString(index);
    }
    
    
    private static float optPixelSize(TypedArray typedArray, int index, float def) {
        if (typedArray == null) {
            return def;
        }
        return typedArray.getDimension(index, def);
    }
    
    private static int optPixelSize(TypedArray typedArray, int index, int def) {
        if (typedArray == null) {
            return def;
        }
        return typedArray.getDimensionPixelOffset(index, def);
    }
    
    private static int optColor(TypedArray typedArray, int index, int def) {
        if (typedArray == null) {
            return def;
        }
        return typedArray.getColor(index, def);
    }
    
    private static boolean optBoolean(TypedArray typedArray, int index, boolean def) {
        if (typedArray == null) {
            return def;
        }
        return typedArray.getBoolean(index, def);
    }
    
    /*******************************************************/
    
    
    private Runnable postPendingDrag = new Runnable() {
        @Override
        public void run() {
            if (!isInAnimating()) {
                pendingDragState();
            }
        }
    };
    
    private ValueAnimator.AnimatorUpdateListener animatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator animation) {
            float value = (Float) animation.getAnimatedValue();
            switch (animateState) {
                case ANIMATE_STATE_PENDING_SETTLE: {
                }
                case ANIMATE_STATE_PENDING_RESET: {
                }
                case ANIMATE_STATE_PENDING_DRAG: {
                    
                    
                    if (animateState != ANIMATE_STATE_PENDING_DRAG) {
                        viewState.buttonStartX = beforeState.buttonStartX
                                + (afterState.buttonStartX - beforeState.buttonStartX) * value;
                    }
                    
                    
                    viewState.leftTitleColor = (int) argbEvaluator.evaluate(
                            value,
                            beforeState.leftTitleColor,
                            afterState.leftTitleColor
                    );
                    ;
                    viewState.rightTitleColor = (int) argbEvaluator.evaluate(
                            value,
                            beforeState.rightTitleColor,
                            afterState.rightTitleColor
                    );
                    ;
                    
                    break;
                }
                case ANIMATE_STATE_SWITCH: {
                    viewState.buttonStartX = beforeState.buttonStartX
                            + (afterState.buttonStartX - beforeState.buttonStartX) * value;
                    
                    float fraction = (viewState.buttonStartX - buttonMinX) / (buttonMaxX - buttonMinX);
                    
                    
                    viewState.leftTitleColor = (int) argbEvaluator.evaluate(
                            fraction,
                            checkedColor,
                            uncheckColor
                    );
                    ;
                    viewState.rightTitleColor = (int) argbEvaluator.evaluate(
                            fraction,
                            uncheckColor,
                            checkedColor
                    );
                    ;
                    
                    break;
                }
                default:
                case ANIMATE_STATE_DRAGING: {
                }
                case ANIMATE_STATE_NONE: {
                    break;
                }
            }
            postInvalidate();
        }
    };
    
    private Animator.AnimatorListener animatorListener
            = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
        }
        
        @Override
        public void onAnimationEnd(Animator animation) {
            switch (animateState) {
                case ANIMATE_STATE_DRAGING: {
                    break;
                }
                case ANIMATE_STATE_PENDING_DRAG: {
                    animateState = ANIMATE_STATE_DRAGING;
                    
                    postInvalidate();
                    break;
                }
                case ANIMATE_STATE_PENDING_RESET: {
                    animateState = ANIMATE_STATE_NONE;
                    postInvalidate();
                    break;
                }
                case ANIMATE_STATE_PENDING_SETTLE: {
                    animateState = ANIMATE_STATE_NONE;
                    postInvalidate();
                    broadcastEvent();
                    break;
                }
                case ANIMATE_STATE_SWITCH: {
//                    isSelectRight = !isSelectRight;
                    animateState = ANIMATE_STATE_NONE;
                    postInvalidate();
                    broadcastEvent();
                    break;
                }
                default:
                case ANIMATE_STATE_NONE: {
                    break;
                }
            }
        }
        
        @Override
        public void onAnimationCancel(Animator animation) {
        }
        
        @Override
        public void onAnimationRepeat(Animator animation) {
        }
    };
    
    
    /*******************************************************/
    /**
     * 保存动画状态
     */
    private static class ViewState {
        /**
         * 按钮x位置[buttonMinX-buttonMaxX]
         */
        float buttonStartX;
        
        int rightTitleColor;
        
        int leftTitleColor;
        
        
        ViewState() {
        }
        
        private void copy(ViewState source) {
            this.buttonStartX = source.buttonStartX;
            this.leftTitleColor = source.leftTitleColor;
            this.rightTitleColor = source.rightTitleColor;
        }
    }
    
}