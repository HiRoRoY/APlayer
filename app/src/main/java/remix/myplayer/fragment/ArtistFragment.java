package remix.myplayer.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import remix.myplayer.R;
import remix.myplayer.adapter.ArtistAdapter;
import remix.myplayer.interfaces.OnItemClickListener;
import remix.myplayer.theme.ThemeStore;
import remix.myplayer.ui.ListItemDecoration;
import remix.myplayer.ui.MultiChoice;
import remix.myplayer.ui.activity.ChildHolderActivity;
import remix.myplayer.ui.activity.MultiChoiceActivity;
import remix.myplayer.util.ColorUtil;
import remix.myplayer.util.Constants;
import remix.myplayer.util.SPUtil;

/**
 * Created by Remix on 2015/12/22.
 */

/**
 * 艺术家Fragment
 */
public class ArtistFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor>{
    @BindView(R.id.artist_recycleview)
    RecyclerView mRecycleView;
    Cursor mCursor = null;
    //艺术家与艺术家id的索引
    public static int mArtistIdIndex = -1;
    public static int mArtistIndex = -1;
    private ArtistAdapter mAdapter;
    public static final String TAG = ArtistFragment.class.getSimpleName();
    private MultiChoice mMultiChoice;

    //列表显示与网格显示切换
    @BindView(R.id.list_model)
    ImageView mListModelBtn;
    @BindView(R.id.grid_model)
    ImageView mGridModelBtn;
    //当前列表模式 1:列表 2:网格
    public static int ListModel = 2;
    private ListItemDecoration mItemDecoration;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getLoaderManager().initLoader(LOADER_ID++, null, this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageName = TAG;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_artist,null);
        mUnBinder = ButterKnife.bind(this,rootView);

        ListModel = SPUtil.getValue(getActivity(),"Setting","ArtistModel",2);
        mRecycleView.setLayoutManager(ListModel == 1 ? new LinearLayoutManager(getActivity()) : new GridLayoutManager(getActivity(), 2));
        mItemDecoration = new ListItemDecoration(getActivity(),ListItemDecoration.VERTICAL_LIST);
        mItemDecoration.setDividerColor(ListModel == Constants.LIST_MODEL ? ThemeStore.getDividerColor() : Color.TRANSPARENT);
        mRecycleView.addItemDecoration(mItemDecoration);
        if(getActivity() instanceof MultiChoiceActivity){
            mMultiChoice = ((MultiChoiceActivity) getActivity()).getMultiChoice();
        }

        mAdapter = new ArtistAdapter(mCursor,getActivity(),mMultiChoice);
        mAdapter.setOnItemClickLitener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                int artistId = getArtsitId(position);
                if(getUserVisibleHint() && artistId > 0 &&
                        !mMultiChoice.itemAddorRemoveWithClick(view,position,artistId,TAG)){
                    if (mCursor.moveToPosition(position)) {
                        int artistid = mCursor.getInt(mArtistIdIndex);
                        String title = mCursor.getString(mArtistIndex);
                        Intent intent = new Intent(getActivity(), ChildHolderActivity.class);
                        intent.putExtra("Id", artistid);
                        intent.putExtra("Title", title);
                        intent.putExtra("Type", Constants.ARTIST);
                        startActivity(intent);
                    }
                }

            }

            @Override
            public void onItemLongClick(View view, int position) {
                int artistId = getArtsitId(position);
                if(getUserVisibleHint() && artistId > 0)
                    mMultiChoice.itemAddorRemoveWithLongClick(view,position,artistId,TAG,Constants.ARTIST);

            }
        });
        mRecycleView.setAdapter(mAdapter);

//        mListModelBtn.setImageDrawable(Theme.getPressAndSelectedStateListDrawalbe(getActivity(),R.drawable.btn_list2));
//        mListModelBtn.setSelected(ListModel == Constants.LIST_MODEL);
//
//        mGridModelBtn.setImageDrawable(Theme.getPressAndSelectedStateListDrawalbe(getActivity(),R.drawable.btn_list1));
//        mGridModelBtn.setSelected(ListModel == Constants.GRID_MODEL);

        mListModelBtn.setColorFilter(ListModel == Constants.LIST_MODEL ? ColorUtil.getColor(R.color.select_model_button_color) : ColorUtil.getColor(R.color.default_model_button_color));
        mGridModelBtn.setColorFilter(ListModel == Constants.GRID_MODEL ? ColorUtil.getColor(R.color.select_model_button_color) : ColorUtil.getColor(R.color.default_model_button_color));
        return rootView;
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
//        CursorLoader loader = new CursorLoader(getActivity(),MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
//                new String[]{BaseColumns._ID,MediaStore.Audio.ArtistColumns.ARTIST},null,null,null);
        return new CursorLoader(getActivity(),MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[]{"distinct " + MediaStore.Audio.Media.ARTIST_ID,MediaStore.Audio.Media.ARTIST},
                Constants.MEDIASTORE_WHERE_SIZE + ")" + " GROUP BY (" + MediaStore.Audio.Media.ARTIST_ID,
                null,
                null);
    }

    public static synchronized int getModel(){
        return ListModel;
    }

    @OnClick({R.id.list_model,R.id.grid_model})
    public void onSwitch(View v){
        int newModel = v.getId() == R.id.list_model ? Constants.LIST_MODEL : Constants.GRID_MODEL;
        if(newModel == ListModel)
            return;

        ListModel = newModel;
        mListModelBtn.setColorFilter(ListModel == Constants.LIST_MODEL ? ColorUtil.getColor(R.color.select_model_button_color) : ColorUtil.getColor(R.color.default_model_button_color));
        mGridModelBtn.setColorFilter(ListModel == Constants.GRID_MODEL ? ColorUtil.getColor(R.color.select_model_button_color) : ColorUtil.getColor(R.color.default_model_button_color));
//        mListModelBtn.setSelected(v.getId() == R.id.list_model);
//        mGridModelBtn.setSelected(v.getId() == R.id.grid_model);

        mRecycleView.setLayoutManager(ListModel == Constants.LIST_MODEL ? new LinearLayoutManager(getActivity()) : new GridLayoutManager(getActivity(), 2));
        mItemDecoration.setDividerColor(ListModel == Constants.LIST_MODEL ? ThemeStore.getDividerColor() : Color.TRANSPARENT);
        SPUtil.putValue(getActivity(),"Setting","ArtistModel",ListModel);
    }

    private int getArtsitId(int position){
        int artistId = -1;
        if(mCursor != null && !mCursor.isClosed() && mCursor.moveToPosition(position)){
            artistId = mCursor.getInt(mArtistIdIndex);
        }
        return artistId;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if(mAdapter != null)
            mAdapter.setCursor(null);
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data != null)
            mCursor = data;
        //设置查询索引
        mArtistIdIndex = data.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID);
        mArtistIndex = data.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        mAdapter.setCursor(data);
    }


    @Override
    public ArtistAdapter getAdapter(){
        return mAdapter;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mCursor != null)
            mCursor.close();
    }

}
