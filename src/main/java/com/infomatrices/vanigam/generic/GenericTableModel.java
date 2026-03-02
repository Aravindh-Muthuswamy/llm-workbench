/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.infomatrices.vanigam.generic;

import javax.swing.table.AbstractTableModel;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.infomatrices.vanigam.annotation.*;
import com.infomatrices.vanigam.arvinsoft.repository.ArvinSoftHibernateRepository;
import com.infomatrices.vanigam.arvinsoft.repository.BaseRepository;
import java.lang.reflect.Method;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import java.util.stream.Stream;

/**
 *
 * @author aravindhmuthuswamy
 */
public class GenericTableModel<T> extends AbstractTableModel {

    private List<T> data;
    private Class<T> entityClass;
    private List<ColumnInfo> columns;
    private boolean includeSerialNumber;
    private final TableContext context;
    private ArvinSoftHibernateRepository<T, ?> repository;

    private static class ColumnInfo {

        Field field;
        Method method;
        String name;
        int order;
        boolean editable;

        ColumnInfo(Field field, JTableColumn annotation, TableContext ctx) {
            this.field = field;
            this.method = null;
            this.name = resolveName(annotation, ctx);
            this.order = annotation.order();
            this.editable = annotation.editable();
            field.setAccessible(true);
        }

        ColumnInfo(Method method, JTableColumn annotation, TableContext ctx) {
            this.field = null;
            this.method = method;
            this.name = resolveName(annotation, ctx);
            this.order = annotation.order();
            this.editable = false; // Methods are read-only
            method.setAccessible(true);
        }

        private static String resolveName(JTableColumn a, TableContext ctx) {
            return switch (ctx) {
                case CUSTOMER ->
                    !a.customer().isEmpty()
                    ? a.customer()
                    : a.name();
                case SUPPLIER ->
                    !a.supplier().isEmpty()
                    ? a.supplier()
                    : a.name();
                default ->
                    a.name();
            };
        }

        Class<?> getType() {
            return field != null ? field.getType() : method.getReturnType();
        }

        Object getValue(Object entity) throws Exception {
            return field != null ? field.get(entity) : method.invoke(entity);
        }

        void setValue(Object entity, Object value) throws Exception {
            if (field != null) {
                field.set(entity, value);
            }
        }
    }

    public GenericTableModel(List<T> data, Class<T> entityClass) {
        this(data, entityClass, true, TableContext.DEFAULT, null);
    }

    public GenericTableModel(
            List<T> data,
            Class<T> entityClass,
            boolean includeSerialNumber,
            TableContext context,
            ArvinSoftHibernateRepository<T, ?> repository
    ) {
        this.data = data != null ? data : new ArrayList<>();
        this.entityClass = entityClass;
        this.includeSerialNumber = includeSerialNumber;
        this.context = context != null ? context : TableContext.DEFAULT;
        this.columns = extractColumns();
        this.repository = repository;
    }
    
    public GenericTableModel(
            List<T> data,
            Class<T> entityClass,
            boolean includeSerialNumber,
            TableContext context
    ) {
        this.data = data != null ? data : new ArrayList<>();
        this.entityClass = entityClass;
        this.includeSerialNumber = includeSerialNumber;
        this.context = context != null ? context : TableContext.DEFAULT;
        this.columns = extractColumns();
        this.repository = null;
    }

    private List<ColumnInfo> extractColumns() {
        Stream<ColumnInfo> fieldColumns = Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(JTableColumn.class))
                .map(field -> new ColumnInfo(field, field.getAnnotation(JTableColumn.class), context));

        Stream<ColumnInfo> methodColumns = Arrays.stream(entityClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(JTableColumn.class))
                .map(method -> new ColumnInfo(method, method.getAnnotation(JTableColumn.class), context));

        return Stream.concat(fieldColumns, methodColumns)
                .sorted((c1, c2) -> Integer.compare(c1.order, c2.order))
                .collect(Collectors.toList());
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size() + (includeSerialNumber ? 1 : 0);
    }

    @Override
    public String getColumnName(int column) {
        if (includeSerialNumber) {
            if (column == 0) {
                return "Sl.No";
            }
            return columns.get(column - 1).name;
        }
        return columns.get(column).name;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (includeSerialNumber) {
            if (columnIndex == 0) {
                return Integer.class;
            }
            return columns.get(columnIndex - 1).getType();
        }
        return columns.get(columnIndex).getType();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (includeSerialNumber) {
            if (columnIndex == 0) {
                return rowIndex + 1;
            }
            try {
                T entity = data.get(rowIndex);
                return columns.get(columnIndex - 1).getValue(entity);
            } catch (Exception e) {
                throw new RuntimeException("Error accessing field/method", e);
            }
        }
        try {
            T entity = data.get(rowIndex);
            return columns.get(columnIndex).getValue(entity);
        } catch (Exception e) {
            throw new RuntimeException("Error accessing field/method", e);
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (includeSerialNumber && columnIndex == 0) {
            return false;
        }
        int actualColumn = includeSerialNumber ? columnIndex - 1 : columnIndex;
        return columns.get(actualColumn).editable;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (includeSerialNumber && columnIndex == 0) {
            return;
        }
        try {
            T entity = data.get(rowIndex);
            int actualColumn = includeSerialNumber ? columnIndex - 1 : columnIndex;
            columns.get(actualColumn).setValue(entity, value);

            // Save using repository
            if (repository != null) {
                repository.update(entity);
            }

            fireTableCellUpdated(rowIndex, columnIndex);
        } catch (Exception e) {
            throw new RuntimeException("Error setting field value", e);
        }
    }

    public void addRow(T entity) {
        data.add(entity);
        fireTableRowsInserted(data.size() - 1, data.size() - 1);
    }

    public void removeRow(int rowIndex) {
        data.remove(rowIndex);
        fireTableRowsDeleted(rowIndex, rowIndex);
    }

    public void removeAllElements() {
        int size = data.size();
        if (size > 0) {
            data.clear();
            fireTableRowsDeleted(0, size - 1);
        }
    }

    public T getEntityAt(int rowIndex) {
        return data.get(rowIndex);
    }

    public void setData(List<T> newData) {
        this.data = newData != null ? newData : new ArrayList<>();
        fireTableDataChanged();
    }

    public List<T> getData() {
        return new ArrayList<>(data);
    }

    public void applySerialNumberRenderer(JTable table) {
        if (includeSerialNumber) {
            DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
            leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
            table.getColumnModel().getColumn(0).setCellRenderer(leftRenderer);
        }
    }
}
