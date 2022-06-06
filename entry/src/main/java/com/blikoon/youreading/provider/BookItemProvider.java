package com.blikoon.youreading.provider;

import com.blikoon.youreading.ResourceTable;
import com.blikoon.youreading.beans.Book_info;
import com.blikoon.youreading.slice.BookDetailSlice;
import com.blikoon.youreading.utils.LoadImageUtil;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.*;
import ohos.app.Context;

import java.util.List;

public class BookItemProvider extends BaseItemProvider {
    private List<Book_info> list;
    private AbilitySlice abilitySlice;

    public BookItemProvider(List<Book_info> list, AbilitySlice abilitySlice) {
        this.list = list;
        this.abilitySlice = abilitySlice;
    }

    public Component getComponent(int i, Component component, ComponentContainer componentContainer){
        Book_info book_info = list.get(i);
        DependentLayout itemLayout = (DependentLayout) LayoutScatter.getInstance(abilitySlice)
                .parse(ResourceTable.Layout_template_book_list_item1,null,false);
        Image image = (Image) itemLayout.findComponentById(ResourceTable.Id_image_book);
        Text text_title = (Text) itemLayout.findComponentById(ResourceTable.Id_book_title);
        Text text_author = (Text) itemLayout.findComponentById(ResourceTable.Id_book_author);
        LoadImageUtil.loadImg(abilitySlice,book_info.getCover_img(),image);
        text_title.setText(book_info.getName());
        text_author.setText(book_info.getAuthor());
        itemLayout.setClickedListener(component1 -> {
            Intent intent = new Intent();
            intent.setParam("ISBN",book_info.getISBN());
            abilitySlice.present(new BookDetailSlice(),intent);
        });
        return itemLayout;
    }
    @Override
    public int getCount() {
        return list.size();
    }
    @Override
    public Object getItem(int i) {
        return list.get(i);
    }
    @Override
    public long getItemId(int i) {
        return 0;
    }
}
