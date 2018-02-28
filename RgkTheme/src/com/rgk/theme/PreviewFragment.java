package com.rgk.theme;

import com.rgk.theme.ImageAdapter.SourceUtil;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PreviewFragment extends Fragment implements OnItemClickListener{

    private static final String TAG = "PreviewFragment";
    private ImageAdapter previewAdapter;
    private float mScale=-1;
    private String mCurrentName;
    private String mThemeName;
    private Drawable mThemeDrawable;
    GridView mPreview;

    private int mScreenWidth;
    private int numColumns=2;//set to gridview for calculate imageview height

    //add for imageview layout
    private int gridmarginLeft;
    private int gridmarginRight;
    private int gridhSpace;
    private static int deviation = 0;

    public static final String THEME_DEFAULT_NAME="System";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        initPreviewData();
         mScreenWidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
         gridmarginLeft = (int)getResources().getDimension(R.dimen.gridview_margin_left);
         gridmarginRight = (int)getResources().getDimension(R.dimen.gridview_margin_right);
         gridhSpace = (int)getResources().getDimension(R.dimen.gridview_hspace);
         deviation =  getResources().getInteger(R.integer.image_deviation);
         Log.d(TAG, "oncreate,and gridmargin is:"+gridmarginLeft+",gridmarginRight is:"+
            gridmarginRight+",gridhSpace is:"+gridhSpace+",mScreenWidth is:"+mScreenWidth+",deviation is:"+deviation);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
        case android.R.id.home:
            // app icon in action bar clicked; go home
            Log.d(TAG, "r.id.home click!");
            getActivity().finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
    }
    }

    private void initPreviewData(){
        previewAdapter = new ImageAdapter(getActivity(), SourceFactory.getSource());
        previewAdapter.setSourceUtil(new SourceUtil() {

            @Override
            public View getView(LayoutInflater inflater, int arg0,
                    View arg1, ViewGroup arg2) {
                // TODO Auto-generated method stub
                   View v = inflater.inflate(R.layout.previewimage, arg2,false);
                   ImageView iv = (ImageView)v.findViewById(R.id.preimage);
                   TextView tv = (TextView)v.findViewById(R.id.themename);
                   ImageView flag = (ImageView)v.findViewById(R.id.flag_use);
                   mCurrentName = Settings.System.getString(getActivity().getContentResolver(), "theme_switch_name");
                   mThemeName = SourceFactory.getSource().get(arg0).getName();
                   if(mCurrentName==null&&mThemeName.equals(THEME_DEFAULT_NAME)){
                       flag.setVisibility(View.VISIBLE);
                   }else if(mCurrentName!=null&&mCurrentName.equals(mThemeName)){
                       flag.setVisibility(View.VISIBLE);
                   }else  flag.setVisibility(View.INVISIBLE);
				   if("System".equals(mThemeName)){
						mThemeName = "Android N";
				   }
                   tv.setText(mThemeName);
                   mThemeDrawable = SourceFactory.getSource().get(arg0).getPreview();
                   if(mScale==-1){
                   int width = mThemeDrawable.getIntrinsicWidth();
                   int  height = mThemeDrawable.getIntrinsicHeight();
                   mScale = (float)height/width;
                   }
                   setPreLayout(iv,mScale);
                   iv.setImageDrawable(mThemeDrawable);
                   return v;
            }
        });
    }

    private void setPreLayout(ImageView target,float scale){
        int totalspace = (numColumns-1)*gridhSpace+gridmarginLeft+gridmarginRight;
        int drawwidth = (mScreenWidth-totalspace)/numColumns;
        int drawheight = (int)(drawwidth*mScale);
        Log.d(TAG, "setPreLayout,and width is:"+drawwidth+",drawheight is:"+drawheight);
        target.setLayoutParams(new FrameLayout.LayoutParams(drawwidth-deviation,drawheight));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        View v = inflater.inflate(R.layout.preview, container,false);
        mPreview = (GridView)v.findViewById(R.id.preview);
        setGridview();//add for set girdview property
        mPreview.setAdapter(previewAdapter);
        mPreview.setOnItemClickListener(this);
        return v;
    }

    private void setGridview(){
        mPreview.setNumColumns(numColumns);
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        mThemeDrawable = null;
        previewAdapter.destroy();
        previewAdapter = null;
        Log.d(TAG, "prefragment,ondestroy!");
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        getActivity().getActionBar().setTitle(R.string.app_name);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        // TODO Auto-generated method stub

         DetailFragment detailFragment = new DetailFragment();
         Bundle data = new Bundle();
         data.putInt(DetailFragment.KEY_POSITION, arg2);
         detailFragment.setArguments(data);
         FragmentTransaction tran = getFragmentManager().beginTransaction();
         tran.addToBackStack(null);
         tran.replace(R.id.main, detailFragment).commit();
    }

}
