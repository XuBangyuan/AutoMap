//package com.baidu.automap.searchroute;
//
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.TextView;
//
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.baidu.automap.R;
//import com.baidu.automap.entity.RoutePlanNode;
//
//import java.util.List;
//
//public class RoutePlanAdapter extends RecyclerView.Adapter implements View.OnClickListener {
//
//    private List<RoutePlanNode> list;
//    private Context context;
//
//    public RoutePlanAdapter(Context context, List<RoutePlanNode> list) {
//        this.context = context;
//        this.list = list;
//    }
//
//    @Override
//    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        View itemView = LayoutInflater.from(parent.getContext())
//                .inflate(R.layout.select_route_node, parent, false);
//        return new ItemHolder(itemView);
//    }
//
//    @Override
//    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
//        ItemHolder itemHolder = (ItemHolder) holder;
//        itemHolder.itemView.setTag(position);
//        itemHolder.setStart.setTag(position);
//        itemHolder.setEnd.setTag(position);
//        itemHolder.deleteNode.setTag(position);
//
//    }
//
//    @Override
//    public long getItemId(int i) {
//        return i;
//    }
//
//    @Override
//    public int getItemCount() {
//        return list.size();
//    }
//
//    public class ItemHolder extends RecyclerView.ViewHolder {
//
//        private RoutePlanNode routeNode;
//
//        private TextView nodeName;
//        private Button setStart;
//        private Button setEnd;
//        private Button deleteNode;
//
//        public ItemHolder(View itemView) {
//            super(itemView);
//
//            nodeName = (TextView) itemView.findViewById(R.id.select_node_name);
//            setStart = (Button) itemView.findViewById(R.id.set_start);
//            setEnd = (Button) itemView.findViewById(R.id.set_end);
//            deleteNode = (Button) itemView.findViewById(R.id.delete_node);
//
//            //将创建的View注册点击事件
//            itemView.setOnClickListener(RoutePlanAdapter.this);
//            setStart.setOnClickListener(RoutePlanAdapter.this);
//            setEnd.setOnClickListener(RoutePlanAdapter.this);
//            deleteNode.setOnClickListener(RoutePlanAdapter.this);
//        }
//    }
//
//
//    ////////////////////////////以下为item点击处理///////////////////////////////
//
//    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
//
//    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
//        this.mOnItemClickListener = listener;
//    }
//
//    /** item里面有多个控件可以点击 */
//    public enum ViewName {
//        SET_START,
//        SET_END,
//        DELETE_NODE,
//        ELSE
//    }
//
//    public interface OnRecyclerViewItemClickListener {
//        void onClick(View view, ViewName viewName, int position);
//    }
//
//    @Override
//    public void onClick(View v) {
//        //注意这里使用getTag方法获取数据
//        int position = (int) v.getTag();
//        if (mOnItemClickListener != null) {
//            switch (v.getId()){
//                case R.id.set_start:
//                    mOnItemClickListener.onClick(v, ViewName.SET_START, position);
//                    break;
//                case R.id.set_end:
//                    mOnItemClickListener.onClick(v, ViewName.SET_END, position);
//                    break;
//                case R.id.delete_node:
//                    mOnItemClickListener.onClick(v, ViewName.DELETE_NODE, position);
//                    break;
//                default:
//                    mOnItemClickListener.onClick(v, ViewName.ELSE, position);
//                    break;
//            }
//        }
//    }
//}