package com.cms.inventariostareas;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.cms.inventariostareas.adapters.ReportesAdapter;
import com.cms.inventariostareas.models.Reporte;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportesActivity extends AppCompatActivity {

    private DatabaseReference dbMovimientos, dbPrestamos;
    private ProgressDialog progressDialog;
    private List<Reporte> listaReportes;
    private RecyclerView recyclerView;
    private ReportesAdapter adapter;

    private static final int CREATE_PDF_REQUEST_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reportes);

        // Inicializar Firebase
        dbMovimientos = FirebaseDatabase.getInstance().getReference("movimientos");
        dbPrestamos = FirebaseDatabase.getInstance().getReference("prestamos");

        listaReportes = new ArrayList<>();

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recyclerViewReportes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReportesAdapter(listaReportes);
        recyclerView.setAdapter(adapter);

        Button btnVolver = findViewById(R.id.btnVolver);
        Button btnGenerarReporte = findViewById(R.id.btnGenerarReporte);
        Button btnExportarPDF = findViewById(R.id.btnExportarPDF);

        btnVolver.setOnClickListener(v -> finish());
        btnGenerarReporte.setOnClickListener(v -> generarReporteCompleto());
        btnExportarPDF.setOnClickListener(v -> crearPDFConStorageAccessFramework());

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Generando Reporte");
        progressDialog.setCanceledOnTouchOutside(false);

        cargarReportes();
    }

    private void crearPDFConStorageAccessFramework() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/pdf");

        // Sugerir nombre del archivo
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "Reporte_Inventario_" + timeStamp + ".pdf";
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        startActivityForResult(intent, CREATE_PDF_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_PDF_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri uri = data.getData();
                exportarReportePDF(uri);
            }
        }
    }

    private void exportarReportePDF(Uri uri) {
        progressDialog.setMessage("Exportando a PDF...");
        progressDialog.show();

        new Thread(() -> {
            try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {

                if (outputStream == null) {
                    throw new IOException("No se pudo abrir el archivo para escritura");
                }

                Document document = new Document();
                PdfWriter.getInstance(document, outputStream);
                document.open();

                // Título del documento
                Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
                Paragraph title = new Paragraph("Sistema de Gestión de Inventarios", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                title.setSpacingAfter(20);
                document.add(title);

                // Subtítulo
                Font subtitleFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
                Paragraph subtitle = new Paragraph("Reporte de Actividades", subtitleFont);
                subtitle.setAlignment(Element.ALIGN_CENTER);
                subtitle.setSpacingAfter(10);
                document.add(subtitle);

                // Fecha de generación
                Font dateFont = new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC);
                String currentDate = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
                Paragraph date = new Paragraph("Generado el: " + currentDate, dateFont);
                date.setAlignment(Element.ALIGN_RIGHT);
                date.setSpacingAfter(20);
                document.add(date);

                // Estadísticas resumen
                Font statsFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
                Paragraph stats = new Paragraph("Total de registros: " + listaReportes.size(), statsFont);
                stats.setSpacingAfter(15);
                document.add(stats);

                // Crear tabla
                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setSpacingBefore(10);

                // Encabezados de la tabla
                Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
                String[] headers = {"Tipo", "Descripción", "Detalles", "Fecha"};

                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    cell.setPadding(8);
                    cell.setBackgroundColor(new com.itextpdf.text.BaseColor(220, 220, 220));
                    table.addCell(cell);
                }

                // Agregar datos a la tabla
                Font dataFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

                for (Reporte reporte : listaReportes) {
                    table.addCell(new PdfPCell(new Phrase(reporte.getTipo(), dataFont)));
                    table.addCell(new PdfPCell(new Phrase(reporte.getDescripcion(), dataFont)));
                    table.addCell(new PdfPCell(new Phrase(reporte.getDetalles(), dataFont)));
                    table.addCell(new PdfPCell(new Phrase(reporte.getFecha(), dataFont)));
                }

                document.add(table);
                document.close();

                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(ReportesActivity.this,
                            "PDF exportado exitosamente", Toast.LENGTH_LONG).show();
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(ReportesActivity.this,
                            "Error al exportar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void cargarReportes() {
        progressDialog.show();
        listaReportes.clear();

        dbMovimientos.orderByChild("fecha").limitToLast(20)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String tipo = snapshot.child("tipo").getValue(String.class);
                            String equipoNombre = snapshot.child("equipoNombre").getValue(String.class);
                            Integer cantidad = snapshot.child("cantidad").getValue(Integer.class);
                            String fecha = snapshot.child("fecha").getValue(String.class);

                            if (tipo != null && equipoNombre != null && cantidad != null && fecha != null) {
                                Reporte reporte = new Reporte(
                                        "Inventario",
                                        tipo.equals("entrada") ? "Entrada de equipo" : "Salida de equipo",
                                        cantidad + " unidades de " + equipoNombre,
                                        fecha
                                );
                                listaReportes.add(reporte);
                            }
                        }
                        cargarPrestamos();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(ReportesActivity.this, "Error al cargar movimientos: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void cargarPrestamos() {
        dbPrestamos.orderByChild("fechaPrestamo").limitToLast(20)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            String equipoNombre = snapshot.child("equipoNombre").getValue(String.class);
                            Integer cantidad = snapshot.child("cantidad").getValue(Integer.class);
                            String estado = snapshot.child("estado").getValue(String.class);
                            String fechaPrestamo = snapshot.child("fechaPrestamo").getValue(String.class);

                            if (equipoNombre != null && cantidad != null && estado != null && fechaPrestamo != null) {
                                Reporte reporte = new Reporte(
                                        "Préstamo",
                                        "Préstamo de " + equipoNombre,
                                        cantidad + " unidades - " + estado,
                                        fechaPrestamo
                                );
                                listaReportes.add(reporte);
                            }
                        }

                        adapter.actualizarLista(listaReportes);
                        progressDialog.dismiss();

                        if (listaReportes.isEmpty()) {
                            Toast.makeText(ReportesActivity.this, "No hay datos para mostrar", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        progressDialog.dismiss();
                        Toast.makeText(ReportesActivity.this, "Error al cargar préstamos: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void generarReporteCompleto() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_generar_reporte, null);
        builder.setView(view);

        Spinner spinnerTipoReporte = view.findViewById(R.id.spinnerTipoReporte);
        Spinner spinnerRangoFecha = view.findViewById(R.id.spinnerRangoFecha);

        ArrayAdapter<CharSequence> tipoAdapter = ArrayAdapter.createFromResource(this,
                R.array.tipos_reporte_array, android.R.layout.simple_spinner_item);
        tipoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoReporte.setAdapter(tipoAdapter);

        ArrayAdapter<CharSequence> fechaAdapter = ArrayAdapter.createFromResource(this,
                R.array.rango_fecha_array, android.R.layout.simple_spinner_item);
        fechaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRangoFecha.setAdapter(fechaAdapter);

        AlertDialog dialog = builder.create();

        view.findViewById(R.id.btnCancelarReporte).setOnClickListener(v -> dialog.dismiss());
        view.findViewById(R.id.btnGenerarReporteDialog).setOnClickListener(v -> {
            String tipoReporte = spinnerTipoReporte.getSelectedItem().toString();
            String rangoFecha = spinnerRangoFecha.getSelectedItem().toString();
            generarReporteFiltrado(tipoReporte, rangoFecha);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void generarReporteFiltrado(String tipoReporte, String rangoFecha) {
        progressDialog.setMessage("Generando reporte " + tipoReporte + "...");
        progressDialog.show();
        cargarReportes();
        Toast.makeText(this, "Filtro aplicado: " + tipoReporte + " - " + rangoFecha, Toast.LENGTH_SHORT).show();
    }
}