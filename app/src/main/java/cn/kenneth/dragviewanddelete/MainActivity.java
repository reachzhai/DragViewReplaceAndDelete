package cn.kenneth.dragviewanddelete;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    DragGridView targetGridView, otherGridView;

    ArrayList<Item> datalist,targetList,otherList;

    private int targetTop;
    private int targetBottom;
    private int targetLeft;
    private int targetRight;

    private int targetPos = -1;

    private int otherTop;
    private int otherBottom;
    private int otherLeft;
    private int otherRight;
    
    private int otherPos = -1;
    
    private TargetAdapter mTargetAdapter;
    private OtherAdapter mOtherAdapter;
    
    private int itemWith;
    private int itemHeight;
    private int bufferWith;
    private int bufferHeigth;
    private int numColum = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        targetGridView = (DragGridView) findViewById(R.id.dragGridView);
        targetGridView.setNumColumns(numColum);
        otherGridView = (DragGridView) findViewById(R.id.dragGridView2);
        otherGridView.setNumColumns(numColum);

        datalist = new ArrayList<>();
        targetList = new ArrayList<>();
        otherList = new ArrayList<>();

        for (int i = 0; i < 20; i++) {
            Item mItem = new Item();
            mItem.setId(i);
            mItem.setName("开关（"+i+"）");
            datalist.add(mItem);
        }
        setData();

        mTargetAdapter = new TargetAdapter(targetList);
        targetGridView.setAdapter(mTargetAdapter);

        mOtherAdapter = new OtherAdapter(otherList);
        otherGridView.setAdapter(mOtherAdapter);

        initParam();
    }
    private void initParam(){
        if (targetBottom != 0) {
            return;
        }
        targetBottom = targetGridView.getBottom();
        targetTop = targetGridView.getTop();
        targetLeft = targetGridView.getLeft();
        targetRight = targetGridView.getRight();

        otherBottom = otherGridView.getBottom();
        otherTop = otherGridView.getTop();
        otherLeft = otherGridView.getLeft();
        otherRight = otherGridView.getRight();

        itemWith = (targetRight - targetLeft) / numColum;
        itemHeight = (targetBottom - targetTop) / (datalist.size() / numColum);
    }

    private void setData(){
        int length = datalist.size();
        for(int i = 0 ;i<length;i++){
            if(i<7){
                targetList.add(datalist.get(i));
            }else{
                otherList.add(datalist.get(i));
            }
        }

    }

    private void dataChange(int targetPos,int otherPos,boolean flag){
        if (targetPos >= targetList.size()
                || otherPos >= otherList.size()) {
            return;
        }

        Item temTargetItem = targetList.get(targetPos);

        targetList.add(targetPos, otherList.get(otherPos));
        targetList.remove(targetPos + 1);

        if(flag) {
            otherList.remove(otherPos);
            otherList.add(0, temTargetItem);
        }else{
            otherList.add(otherPos, temTargetItem);
            otherList.remove(otherPos+1);
        }

        mTargetAdapter.notifyDataSetChanged();
        mOtherAdapter.notifyDataSetChanged();
    }


    private class OtherAdapter extends BaseAdapter implements DragGridView.DragGridBaseAdapter {
        private ArrayList<Item> mList;
        public int mHidePosition = -1;

        private int colors[] = {android.R.color.holo_red_light, android.R.color.holo_blue_light, android.R.color.holo_orange_light};

        public OtherAdapter(ArrayList<Item> list) {
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Item getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item, parent, false);
                holder = MyHolder.create(convertView);
                convertView.setTag(holder);
            } else {
                holder = (MyHolder) convertView.getTag();
            }

            holder.mTextView.setText(getItem(position).getName().toString());
            holder.mImageView.setImageResource(colors[getItem(position).getId() % 3]);
            //隐藏被拖动的
            if (position == mHidePosition) {
                convertView.setVisibility(View.INVISIBLE);
            } else {
                convertView.setVisibility(View.VISIBLE);
            }

            return convertView;
        }


        @Override
        public void reorderItems(int oldPosition, int newPosition) {
            Item temp = mList.get(oldPosition);
            if (oldPosition < newPosition) {
                for (int i = oldPosition; i < newPosition; i++) {
                    Collections.swap(mList, i, i + 1);
                }
            } else if (oldPosition > newPosition) {
                for (int i = oldPosition; i > newPosition; i--) {
                    Collections.swap(mList, i, i - 1);
                }
            }

            mList.set(newPosition, temp);
        }

        @Override
        public void dragItems(int x,int y) {

        }
        @Override
        public void setHideItem(int hidePosition) {
            mHidePosition = hidePosition;
            notifyDataSetChanged();
        }

        @Override
        public void deleteItem(int deletePosition) {
            if (null != mList && deletePosition < mList.size()) {
                mList.remove(deletePosition);
                notifyDataSetChanged();
            }
        }

        @Override
        public void releaseItem(int x, int y, int curPostion) {
            initParam();

            Log.e("zhailong", "itemWith : "+itemWith);
            if ((y > targetTop - 100)
                    && (y < targetBottom - 100)
                    && (x > targetLeft - itemWith)
                    && (x < targetRight + itemWith)) {

                int temX = x - targetLeft + 50;
                int pos = temX / itemWith;

                Log.e("zhailong", "releaseItem ===x: " + x + " ===y: " + y + " ===itemWith: " + itemWith + " ===pos: " + pos + " dragPos:" + curPostion);

                targetPos = pos;
                dataChange(targetPos, curPostion, true);

            } else {
                targetPos = -1;
            }
        }
    }

    private class TargetAdapter extends BaseAdapter implements DragGridView.DragGridBaseAdapter {
        private ArrayList<Item> mList;
        public int mHidePosition = -1;

        private int colors[] = {android.R.color.holo_red_light, android.R.color.holo_blue_light, android.R.color.holo_orange_light};

        public TargetAdapter(ArrayList<Item> list) {
            mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Item getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            MyHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item, parent, false);
                holder = MyHolder.create(convertView);
                convertView.setTag(holder);
            } else {
                holder = (MyHolder) convertView.getTag();
            }

            holder.mTextView.setText(getItem(position).getName().toString());
            holder.mImageView.setImageResource(colors[getItem(position).getId() % 3]);
            //隐藏被拖动的
            if (position == mHidePosition) {
                convertView.setVisibility(View.INVISIBLE);
            } else {
                convertView.setVisibility(View.VISIBLE);
            }

            return convertView;
        }


        @Override
        public void reorderItems(int oldPosition, int newPosition) {
            Item temp = mList.get(oldPosition);
            if (oldPosition < newPosition) {
                for (int i = oldPosition; i < newPosition; i++) {
                    Collections.swap(mList, i, i + 1);
                }
            } else if (oldPosition > newPosition) {
                for (int i = oldPosition; i > newPosition; i--) {
                    Collections.swap(mList, i, i - 1);
                }
            }

            mList.set(newPosition, temp);
        }

        @Override
        public void dragItems(int x,int y) {
            initParam();
//            if (y>otherTop && y<otherBottom
//                    && x>otherLeft && x<otherRight) {
//                int temX = x - otherLeft;
//                int itemWith = (otherRight - otherLeft)/7;
//                int pos = temX/itemWith;
//                Log.e("zhailong other","===x: " + x + " ===y: " + y + " ===itemWith: " + itemWith + " ===pos: " + pos);
//                otherPos = pos;
//            }else{
//                otherPos = -1;
//            }
        }
        @Override
        public void setHideItem(int hidePosition) {
            mHidePosition = hidePosition;
            notifyDataSetChanged();
        }

        @Override
        public void deleteItem(int deletePosition) {
//            if (null != mList && deletePosition < mList.size()) {
//                mList.remove(deletePosition);
//                notifyDataSetChanged();
//            }
        }

        @Override
        public void releaseItem(int x, int y, int curPostion) {
            initParam();
            if ((y > otherTop - 100)
                    && (y < otherBottom - 100)
                    && (x > otherLeft - itemWith)
                    && (x < otherRight + itemWith)) {

                int temX = x - otherLeft + 50;
                int pos = temX / itemWith;

                int temY = y - otherTop + 50;
                int line = temY / itemHeight;

                Log.e("zhailong other", "===temY: " + temY + " ===y: " + y + " ===itemHeight: " + itemHeight + " ===pos: " + pos + " === line: " + line);

                otherPos = line * numColum + pos;
                if(otherPos>mList.size()){
                    otherPos = otherPos - numColum;
                }

                dataChange(curPostion, otherPos, false);
            } else {
                otherPos = -1;
            }
        }
    }

    private static class MyHolder {
        public ImageView mImageView;
        public TextView mTextView;


        public MyHolder(ImageView imageView, TextView textView) {
            this.mImageView = imageView;
            this.mTextView = textView;
        }


        public static MyHolder create(View rootView) {
            ImageView image = (ImageView) rootView.findViewById(R.id.image);
            TextView text = (TextView) rootView.findViewById(R.id.text);
            return new MyHolder(image, text);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
