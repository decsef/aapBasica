package com.curso;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.luciad.datamodel.*;
import com.luciad.model.*;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.shape.ILcdPoint;
import com.luciad.shape.TLcdShapeDataTypes;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.util.TLcdHasGeometryAnnotation;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class AlbergueModelDecoder implements ILcdModelDecoder {
    public static final TLcdDataModel DATA_MODEL;
    public static final TLcdDataType ALBERGUE_TIPO;

    static {
        TLcdDataModelBuilder dataModelBuilder = new TLcdDataModelBuilder("albergueModelConstructor");

        TLcdDataTypeBuilder alberguePointTypeBuilder = dataModelBuilder.typeBuilder("puntoAlbergue")
                .superType(TLcdShapeDataTypes.SHAPE_TYPE)
                .primitive(true).instanceClass(TLcdLonLatHeightPoint.class);

        TLcdDataTypeBuilder albergueTypeBuilder = dataModelBuilder.typeBuilder("AlbergueType");
        albergueTypeBuilder.addProperty("idAlbergue", TLcdCoreDataTypes.INTEGER_TYPE);
        albergueTypeBuilder.addProperty("idEstado", TLcdCoreDataTypes.INTEGER_TYPE);
        albergueTypeBuilder.addProperty("idMunicipio", TLcdCoreDataTypes.INTEGER_TYPE);
        albergueTypeBuilder.addProperty("albergueNombre", TLcdCoreDataTypes.STRING_TYPE);
        albergueTypeBuilder.addProperty("puntoAlbergue", alberguePointTypeBuilder);
        // Crea Modelo de Datos.
        TLcdDataModel dataAlbergueModel = dataModelBuilder.createDataModel();
        // Especifica qué propiedad tiene la geometría
        TLcdDataType alberguePointType = dataAlbergueModel.getDeclaredType("AlbergueType");
        alberguePointType.addAnnotation(new TLcdHasGeometryAnnotation(alberguePointType.getProperty("puntoAlbergue")));
        // ASIGNA VALORES A VARIABLES ESTÁTICAS FINALES DE LA CLASE.
        DATA_MODEL = dataAlbergueModel;
        ALBERGUE_TIPO = alberguePointType;
    }


    @Override
    public String getDisplayName() {
        return "Albergues";
    }

    @Override
    public boolean canDecodeSource(String aOrigenDatos) {
        JsonParser jsonParserDecode = new JsonParser();
        return aOrigenDatos != null || jsonParserDecode.parse(aOrigenDatos).isJsonArray();
    }

    /**
     * @param urlServer
     *
     * @return
     * @throws IOException
     */
    @Override
    public ILcdModel decode(String urlServer) throws IOException {
        TLcd2DBoundsIndexedModel indexedModel = creaModeloVacio();
        indexedModel.setModelReference(creaReferenciaModelo());

        ILcdModelDescriptor descripcionModel = creaDescripcionModel(urlServer);
        indexedModel.setModelDescriptor(descripcionModel);

        List<ILcdDataObject> albergues = creaAlbergues(urlServer);
        indexedModel.addElements(new Vector<>(albergues), ILcdModel.NO_EVENT);
        return indexedModel;
    }

    private List<ILcdDataObject> creaAlbergues(String sUrlServer) throws IOException {
        List<ILcdDataObject> result = new ArrayList<>();
        try {
            HttpRequest requestHTTP = HttpRequest.newBuilder().uri(new URI(sUrlServer)).header("Content-Type", "application/json").GET().build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(requestHTTP, HttpResponse.BodyHandlers.ofString());

            if (!canDecodeSource(response.body())) {
                throw new IOException("No se pudo decodificar la fuente" + sUrlServer);
            }

            JsonParser jsonParser = new JsonParser();
            JsonArray jsonArray = jsonParser.parse(response.body()).getAsJsonArray();

            for (JsonElement jObj : jsonArray) {
                JsonObject jsonObject = jObj.getAsJsonObject();
                ILcdPoint creaPunto = new TLcdLonLatHeightPoint(jsonObject.get("longitud").getAsDouble(), jsonObject.get("latitud").getAsDouble(), 0);

                ILcdDataObject albergueJson = ALBERGUE_TIPO.newInstance();
                albergueJson.setValue("idAlbergue", jsonObject.get("idAlbergue").getAsInt());
                albergueJson.setValue("idEstado", jsonObject.get("idEntidad").getAsInt());
                albergueJson.setValue("idMunicipio", jsonObject.get("idMunicipio").getAsInt());
                albergueJson.setValue("albergueNombre", jsonObject.get("nombreAlbergue").getAsString());
                albergueJson.setValue("puntoAlbergue", creaPunto);
                result.add(albergueJson);
            }
            return result;

        } catch (URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private ILcdModelDescriptor creaDescripcionModel(String sOrigenDatos) {
        return new TLcdDataModelDescriptor(sOrigenDatos, "json", "albergues", DATA_MODEL, Collections.singleton(ALBERGUE_TIPO), DATA_MODEL.getTypes());
    }

    private ILcdModelReference creaReferenciaModelo() {
        return new TLcdGeodeticReference();
    }

    private TLcd2DBoundsIndexedModel creaModeloVacio() {
        return new TLcd2DBoundsIndexedModel();
    }
}
