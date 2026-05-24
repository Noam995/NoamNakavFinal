package com.example.noamnakavfinal.adapter;

// ייבוא מחלקות נדרשות של אנדרואיד (תצוגה, רשימות) ושל הפרויקט (מודל המכירה)
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.noamnakavfinal.R;
import com.example.noamnakavfinal.model.Sale;
import java.util.List;

// מחלקת המתאם עבור היסטוריית המכירות. יורשת מ-RecyclerView.Adapter ומשתמשת ב-SaleViewHolder
public class SalesAdapter extends RecyclerView.Adapter<SalesAdapter.SaleViewHolder> {

    // רשימה שתשמור בתוכה את כל אובייקטי המכירות (Sale) שצריך להציג
    private List<Sale> salesList;

    // --- פעולה בונה (Constructor) ---
    // מקבלת את רשימת המכירות מבחוץ (ממסך SalesHistoryActivity) ושומרת אותה במשתנה המקומי
    public SalesAdapter(List<Sale> salesList) {
        this.salesList = salesList;
    }

    // --- יצירת תבנית התצוגה (ViewHolder) ---
    // פונקציה זו מופעלת בכל פעם שהרשימה צריכה לייצר "שורה" חדשה על המסך
    @NonNull
    @Override
    public SaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // מנפח (טוען) קובץ עיצוב.
        // שים לב: כאן השתמשת בעיצוב מובנה של אנדרואיד (simple_list_item_2)
        // ולא בקובץ XML שיצרת בעצמך. העיצוב הזה מכיל פשוט שתי תיבות טקסט אחת מעל השנייה.
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);

        // מחזיר את התצוגה עטופה ב-ViewHolder שמחזיק את הרכיבים שלה
        return new SaleViewHolder(view);
    }

    // --- חיבור הנתונים לתצוגה ---
    // מופעלת עבור כל שורה כדי לשפוך אליה את הנתונים המדויקים של אותה מכירה
    @Override
    public void onBindViewHolder(@NonNull SaleViewHolder holder, int position) {
        // שליפת המכירה הספציפית מהרשימה לפי המיקום (position) שלה
        Sale sale = salesList.get(position);

        // הצבת נתוני הרכב (יצרן ודגם) בתיבת הטקסט הראשונה (העליונה והמודגשת יותר)
        holder.text1.setText(sale.getCar().getBrand() + " " + sale.getCar().getModel());

        // הצבת תאריך העסקה והמחיר בתיבת הטקסט השנייה (התחתונה והקטנה יותר)
        holder.text2.setText("תאריך: " + sale.getDate() + " | מחיר: ₪" + sale.getPrice());
    }

    // --- ספירת פריטים ---
    // מחזירה למערכת כמה פריטים (מכירות) קיימים בסך הכל ברשימה
    @Override
    public int getItemCount() {
        return salesList.size();
    }

    // --- מחלקה פנימית: SaleViewHolder ---
    // שומרת "מטמון" (Cache) של רכיבי התצוגה (תיבות הטקסט) כדי שלא נצטרך לחפש אותם בכל פעם שגוללים את המסך
    static class SaleViewHolder extends RecyclerView.ViewHolder {

        // שתי תיבות טקסט - תואמות לעיצוב המובנה simple_list_item_2
        TextView text1, text2;

        // פעולה בונה שמאתרת את תיבות הטקסט מתוך השורה הפיזית
        public SaleViewHolder(@NonNull View itemView) {
            super(itemView);
            // מציאת תיבות הטקסט לפי ה-ID המובנה של אנדרואיד (android.R.id.text1/2)
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);
        }
    }
}