package app.organicmaps.editor;

import android.content.res.Configuration;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import app.organicmaps.R;
import app.organicmaps.sdk.editor.data.HoursMinutes;
import app.organicmaps.sdk.util.DateUtils;

public class FromToTimePicker
{
  private final FragmentActivity mActivity;
  private final FragmentManager mFragmentManager;
  private final OnPickListener mListener;
  private final int mId;
  private final boolean mIs24HourFormat;
  private final Resources mResources;

  private HoursMinutes mFromTime;
  private HoursMinutes mToTime;
  private MaterialTimePicker mToTimePicker;
  private MaterialTimePicker mFromTimePicker;
  private boolean mIsFromTimePicked;

  public static void pickTime(@NonNull Fragment fragment,
                              @NonNull FromToTimePicker.OnPickListener listener,
                              @NonNull HoursMinutes fromTime,
                              @NonNull HoursMinutes toTime,
                              int id,
                              boolean startWithToTime)
  {
    FromToTimePicker timePicker = new FromToTimePicker(fragment,
                                                       listener,
                                                       fromTime,
                                                       toTime,
                                                       id);

    if (startWithToTime)
      timePicker.showToTimePicker();
    else
      timePicker.showFromTimePicker();
  }

  private FromToTimePicker(@NonNull Fragment fragment,
                           @NonNull FromToTimePicker.OnPickListener listener,
                           @NonNull HoursMinutes fromTime,
                           @NonNull HoursMinutes toTime,
                           int id)
  {
    mActivity = fragment.requireActivity();
    mFragmentManager = fragment.getChildFragmentManager();
    mListener = listener;
    mFromTime = fromTime;
    mToTime = toTime;
    mId = id;

    mIsFromTimePicked = false;

    mIs24HourFormat = DateUtils.is24HourFormat(mActivity);
    mResources = mActivity.getResources();

    mActivity.addOnConfigurationChangedListener(this::handleConfigurationChanged);
  }

  public void showFromTimePicker()
  {
    if (mFromTimePicker != null)
    {
      mFromTime = getHoursMinutes(mFromTimePicker);
      mFromTimePicker.dismiss();
    }

    mFromTimePicker = buildFromTimePicker();
    mFromTimePicker.show(mFragmentManager, null);
  }

  public void showToTimePicker()
  {
    if (mToTimePicker != null)
    {
      mToTime = getHoursMinutes(mToTimePicker);
      mToTimePicker.dismiss();
    }

    mToTimePicker = buildToTimePicker();

    mToTimePicker.show(mFragmentManager, null);
  }

  private MaterialTimePicker buildFromTimePicker()
  {
    MaterialTimePicker timePicker = buildTimePicker(mFromTime,
                                                    mResources.getString(R.string.editor_time_from),
                                                    mResources.getString(R.string.next_button),
                                                    null);

    timePicker.addOnNegativeButtonClickListener(view -> finishTimePicking(false));

    timePicker.addOnPositiveButtonClickListener(view ->
    {
      mIsFromTimePicked = true;
      mFromTime = getHoursMinutes(timePicker);
      showToTimePicker();
    });

    timePicker.addOnCancelListener(view -> finishTimePicking(false));

    return timePicker;
  }

  private MaterialTimePicker buildToTimePicker()
  {
    MaterialTimePicker timePicker = buildTimePicker(mToTime,
                                                    mResources.getString(R.string.editor_time_to),
                                                    null,
                                                    mResources.getString(R.string.back));

    timePicker.addOnNegativeButtonClickListener(view ->
    {
      if (mIsFromTimePicked)
        showFromTimePicker();
      else
        finishTimePicking(false);
    });

    timePicker.addOnPositiveButtonClickListener(view ->
    {
      mToTime = getHoursMinutes(timePicker);
      finishTimePicking(true);
    });

    timePicker.addOnCancelListener(view -> finishTimePicking(false));

    return timePicker;
  }

  @NonNull
  private MaterialTimePicker buildTimePicker(@NonNull HoursMinutes time,
                                             @NonNull String title,
                                             @Nullable String positiveButtonTextOverride,
                                             @Nullable String negativeButtonTextOverride)
  {
    MaterialTimePicker.Builder builder = new MaterialTimePicker.Builder()
                                             .setTitleText(title)
                                             .setTimeFormat(mIs24HourFormat ? TimeFormat.CLOCK_24H : TimeFormat.CLOCK_12H)
                                             .setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK)
                                             .setTheme(R.style.MwmMain_MaterialTimePicker)
                                             .setHour((int) time.hours)
                                             .setMinute((int) time.minutes);

    if (positiveButtonTextOverride != null)
      builder.setPositiveButtonText(positiveButtonTextOverride);

    if (negativeButtonTextOverride != null)
      builder.setNegativeButtonText(negativeButtonTextOverride);

    return builder.build();
  }

  private HoursMinutes getHoursMinutes(@NonNull MaterialTimePicker picker)
  {
    return new HoursMinutes(picker.getHour(), picker.getMinute(), mIs24HourFormat);
  }

  private void finishTimePicking(boolean isConfirmed)
  {
    mActivity.removeOnConfigurationChangedListener(this::handleConfigurationChanged);

    if (isConfirmed)
      mListener.onHoursMinutesPicked(mFromTime, mToTime, mId);
  }

  private void handleConfigurationChanged(Configuration configuration)
  {
    if (mFromTimePicker != null && mFromTimePicker.isVisible())
      showFromTimePicker();
    else if (mToTimePicker != null && mToTimePicker.isVisible())
      showToTimePicker();
  }

  public interface OnPickListener
  {
    void onHoursMinutesPicked(HoursMinutes from, HoursMinutes to, int id);
  }
}
