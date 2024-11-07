package com.curso;

import com.curso.paneles.LspLayerSelectionPanel;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luciad.format.asterix.*;
import com.luciad.format.kml22.view.lightspeed.TLspKML22LayerBuilder;
import com.luciad.format.raster.TLcdGeoTIFFModelDecoder;
import com.luciad.format.shp.TLcdSHPModelDecoder;
import com.luciad.geodesy.TLcdGeodeticDatum;
import com.luciad.gui.TLcdAnchoredIcon;
import com.luciad.gui.TLcdImageIcon;
import com.luciad.gui.TLcdSVGIcon;
import com.luciad.model.*;
import com.luciad.ogc.wfs.client.TLcdWFSClient;
import com.luciad.ogc.wfs.client.TLcdWFSDataSource;
import com.luciad.ogc.wfs.common.model.TLcdWFSCapabilities;
import com.luciad.ogc.wfs.common.model.TLcdWFSFeatureType;
import com.luciad.ogc.wfs.common.model.TLcdWFSFeatureTypeList;
import com.luciad.projection.TLcdEquidistantCylindrical;
import com.luciad.realtime.lightspeed.radarvideo.TLspRadarVideoLayerBuilder;
import com.luciad.reference.TLcdGeodeticReference;
import com.luciad.reference.TLcdGridReference;
import com.luciad.shape.shape3D.TLcdLonLatHeightPoint;
import com.luciad.symbology.app6a.model.ELcdAPP6Standard;
import com.luciad.symbology.app6a.model.ILcdAPP6ACoded;
import com.luciad.symbology.app6a.model.TLcdAPP6AModelDescriptor;
import com.luciad.symbology.app6a.model.TLcdEditableAPP6AObject;
import com.luciad.symbology.app6a.view.lightspeed.TLspAPP6ALayerBuilder;
import com.luciad.symbology.milstd2525b.model.ELcdMS2525Standard;
import com.luciad.symbology.milstd2525b.model.ILcdMS2525bCoded;
import com.luciad.symbology.milstd2525b.model.TLcdEditableMS2525bObject;
import com.luciad.symbology.milstd2525b.model.TLcdMS2525bModelDescriptor;
import com.luciad.symbology.milstd2525b.view.lightspeed.TLspMS2525bLayerBuilder;
import com.luciad.util.ILcdFireEventMode;
import com.luciad.util.collections.ILcdList;
import com.luciad.util.collections.TLcdArrayList;
import com.luciad.util.service.TLcdServiceLoader;
import com.luciad.view.ILcdLayer;
import com.luciad.view.ILcdLayerTreeNode;
import com.luciad.view.TLcdLayer;
import com.luciad.view.TLcdLayerTreeNodeUtil;
import com.luciad.view.lightspeed.ILspAWTView;
import com.luciad.view.lightspeed.ILspPaintingOrder;
import com.luciad.view.lightspeed.TLspContext;
import com.luciad.view.lightspeed.TLspViewBuilder;
import com.luciad.view.lightspeed.layer.*;
import com.luciad.view.lightspeed.layer.raster.TLspRasterLayer;
import com.luciad.view.lightspeed.layer.shape.TLspShapeLayerBuilder;
import com.luciad.view.lightspeed.painter.grid.TLspLonLatGridLayerBuilder;
import com.luciad.view.lightspeed.painter.label.style.TLspDataObjectLabelTextProviderStyle;
import com.luciad.view.lightspeed.style.*;
import com.luciad.view.lightspeed.style.styler.ALspStyleCollector;
import com.luciad.view.lightspeed.style.styler.ALspStyler;
import com.luciad.view.lightspeed.style.styler.ILspStyler;
import com.luciad.view.lightspeed.util.TLspViewTransformationUtil;
import com.luciad.view.swing.TLcdLayerTree;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import javax.accessibility.AccessibleIcon;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import javax.xml.namespace.QName;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Timer;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


/**
 * Código desarrollado durante el trainning de Luciad en Madrid, España.
 *
 * @author RobertoColorado
 * created 29/oct/2024
 */
public class Main {
    public static final ILspAWTView vista = TLspViewBuilder.newBuilder().buildAWTView();
    private static final String TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJsb3JlIiwiaWF0IjoxNzIyMDE5MTgxfQ.Qt--n_TPitKsoHC2ZsD0uhA7k0rzDCd866uekR28JKR_KP36fhr3I3dQreglblYo_Pc-F-pCssIeSNf85MuTwQ";
    private static final String URL = "http://155.138.216.49:8080/ollin-server/api/socket";
    private final ILcdList<ILcdLayer> fSelectedLayers = new TLcdArrayList<>();
    private final ConcurrentHashMap<String, Track> trackData = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Timer> trackTimers = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<String> messageBuffer = new ConcurrentLinkedQueue<>();
    public ILspLayer fSelectedLayer;

    /**
     * Clase Main, punto de acceso
     * @param args
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            JFrame frame = new Main().creaInterfaz();
            frame.setVisible(true);
        });
    }

    /**
     * Metodo privado que crea la interfaz que se llama desde el metodo principal
     *
     * @return JFrame de Java con la interfaz gráfica que contiene la vista de LuciadLightspeed.
     */
    private JFrame creaInterfaz() {
        JFrame frame = new JFrame("Aplicación básica de Lightspeed");
        frame.setSize(2000, 1500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Crea Vista de LuciadLightspeed y se agrega al JFrame
        frame.add(vista.getHostComponent(), BorderLayout.CENTER);
        //fLayerSelectionPanel = new LspLayerSelectionPanel(vista, true);
        // frame.add(fLayerSelectionPanel, BorderLayout.EAST);

        // Metodo que manda la vista que se utiliza para carga toda la información mediante los layers de LLS.
        agregaData();

        // Crea árbol de las capas que contiene la vista de LLS y la añade al frame de lado derecho (EAST)
        JComponent arbolCapas = creaArbolCapas();
        Icon jIcon = new ImageIcon("png/arriba.png");

        JButton upJButton = subeCapa();
        //arbolCapas.add(upJButton);
        frame.add(arbolCapas, BorderLayout.EAST);
       // frame.add(upJButton, BorderLayout.SOUTH);
        // crea los botónes de 2d/3d que permiten cambiar la vista del mundo. le agrega dos radio buttons y llama métodos para modificar vista.
        JToolBar jToolBar = creaToolBar();
        jToolBar.add(upJButton);
        frame.add(jToolBar, BorderLayout.NORTH);

        return frame;
    }

    /**
     * Crea la barra de herramientas con los radio botones que cambian las vista del mapa 2D/3D
     *
     * @return Jtoolbar con los botones y su funcionalidad de cambiar vista.
     */
    private JToolBar creaToolBar() {
        JToolBar toolBar = new JToolBar();
        JRadioButton b2d = new JRadioButton(createSwitchTo2DAction());
        b2d.setSelected(true);
        JRadioButton b3d = new JRadioButton(createSwitchTo3DAction());
        ButtonGroup group = new ButtonGroup();
        group.add(b2d);
        group.add(b3d);
        toolBar.add(b2d);
        toolBar.add(b3d);
        return toolBar;
    }

    /**
     * Crea Árbol de Capas.
     * @return
     */
    private ILcdLayerTreeNode fSelectedLayerNode; // Variable de instancia para nodos de capa

    private JComponent creaArbolCapas() {
        TLcdLayerTree jArbol = new TLcdLayerTree(vista);
        jArbol.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        jArbol.addTreeSelectionListener(e -> {
            List<Object> selectedNodes = jArbol.storeSelectedNodes();
            if (!selectedNodes.isEmpty()) {
                Object selectedNode = selectedNodes.get(0);
                if (selectedNode instanceof ILspLayer) {
                    fSelectedLayer = (ILspLayer) selectedNode;
                    fSelectedLayerNode = null;
                } else if (selectedNode instanceof ILcdLayerTreeNode) {
                    fSelectedLayerNode = (ILcdLayerTreeNode) selectedNode;
                    fSelectedLayer = null;
                } else {
                    fSelectedLayer = null;
                    fSelectedLayerNode = null;
                }
            } else {
                fSelectedLayer = null;
                fSelectedLayerNode = null;
            }
        });
        return jArbol;
    }


    private JButton subeCapa() {
        Icon jIcon = new ImageIcon("png/arriba.png");
        JButton upJButton = new JButton(jIcon);
        upJButton.addActionListener(e -> {
            if (fSelectedLayer != null) {
                ILcdLayerTreeNode parentNode = TLcdLayerTreeNodeUtil.getParent(fSelectedLayer, vista.getRootNode());
                if (parentNode != null) {
                    parentNode.moveLayerAt(parentNode.layerCount() - 1, fSelectedLayer);
                } else {
                    vista.moveLayerAt(vista.layerCount() - 1, fSelectedLayer);
                }
            } else if (fSelectedLayerNode != null) {
                ILcdLayerTreeNode parentNode = TLcdLayerTreeNodeUtil.getParent(fSelectedLayerNode, vista.getRootNode());
                if (parentNode != null) {
                    parentNode.moveLayerAt(parentNode.layerCount() - 1, fSelectedLayerNode);
                } else {
                    vista.moveLayerAt(vista.layerCount() - 1, fSelectedLayerNode);
                }
            }
        });
        return upJButton;
    }


    /**
     * Metodo que agrupa la creación de modelos y capas que se añaden a la vista.
     * Maneja todas las excepciones de los métodos que usan entradas/salidas.
     */
    private void agregaData() {
        try {
            // Carga información del mapa base.
            //agregaGeotiffData();
            // Carga información del mapa base.
            agregaShapeData();
            // Carga Grid
            agregaGridLayer();
            //Carga información del TOMCAT, albergues
            agregaAlberguesLayer();
            agregaKMZLayer();
            // Cargar información de WMS en Geoserver
            // agregaWFS();
            //agregaFlightRadar();
            //cargaTracksOlin();
            //simbologiaMilitar();
            agregarAstrix();

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void agregarAstrix() {
        ILcdModelDecoder decoder = new TLcdCompositeModelDecoder(TLcdServiceLoader.getInstance(ILcdModelDecoder.class));
        ILcdModel model = null;
        try {
            model = decoder.decode("ASTERIX/atx_cat21.astfin");
//            System.out.println(model);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Create a layer for the model
        ILspLayer layer = createAsterixLayer(model);

        //Add the layer to the Lightspeed view (an ILspView)
        vista.addLayer(layer);

    }

    static ILspLayer createAsterixLayer(ILcdModel model) {
        //The model container itself does not contain any data,
        //so we create an empty layer node for it
        if (model instanceof ILcdModelContainer) {
            TLspLayerTreeNode node = new TLspLayerTreeNode(model);
            for (int i = 0; i < ((ILcdModelContainer) model).modelCount(); i++) {
                ILcdModel childModel = ((ILcdModelContainer) model).getModel(i);
                //Use recursion to create layers for the child models
                node.addLayer(createAsterixLayer(childModel));
            }
            return node;
        } else if (model.getModelDescriptor() instanceof TLcdASTERIXRadarVideoModelDescriptor) {
            //Use the radar layer builder to create the layer
            return TLspRadarVideoLayerBuilder.newBuilder()
                    .model(model)
                    .selectable(false)
                    .build();
        } else if (model.getModelDescriptor() instanceof TLcdASTERIXWeatherModelDescriptor) {
            return createWeatherLayer(model);
        } else if (model.getModelDescriptor() instanceof TLcdASTERIXPlotModelDescriptor ||
                model.getModelDescriptor() instanceof TLcdASTERIXTrajectoryModelDescriptor ||
                model.getModelDescriptor() instanceof TLcdASTERIXTrackModelDescriptor) {
            return TLspShapeLayerBuilder.newBuilder()
                    .model(model)
                    .build();
        }
        throw new IllegalArgumentException("Cannot create layer for model " + model);
    }

    private static ILspLayer createWeatherLayer(ILcdModel model) {
        ILspStyler weatherStyler = new ALspStyler() {

            private final List<TLspLineStyle> fLineStyles = Arrays.asList(
                    TLspLineStyle.newBuilder().color(new Color(231, 232, 255)).build(),
                    TLspLineStyle.newBuilder().color(new Color(157, 162, 255)).build(),
                    TLspLineStyle.newBuilder().color(new Color(0, 9, 120)).build(),
                    TLspLineStyle.newBuilder().color(new Color(255, 112, 112)).build(),
                    TLspLineStyle.newBuilder().color(new Color(255, 51, 51)).build(),
                    TLspLineStyle.newBuilder().color(new Color(190, 0, 0)).build(),
                    TLspLineStyle.newBuilder().color(new Color(216, 0, 202)).build()
            );

            @Override
            public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
                for (Object object : aObjects) {
                    //Customize the color based on the intensity of the precipitation of the weather data
                    int intensity = ((TLcdASTERIXPrecipitationZone) object).getIntensity();

                    //Intensity level 0 signifies no precipitation, so we will only paint levels 1 through 7.
                    if (intensity == 0) {
                        continue;
                    }
                    aStyleCollector.object(object).style(fLineStyles.get(intensity - 1)).submit();
                }
            }
        };

        return TLspShapeLayerBuilder.newBuilder()
                .model(model)
                .bodyStyler(TLspPaintState.REGULAR, weatherStyler)
                .build();
    }

    private void agregaKMZLayer() throws IOException {
        TLspLayerTreeNode kmdNode = new TLspLayerTreeNode("KML");
        TLcdCompositeModelDecoder decoderKml = new TLcdCompositeModelDecoder(TLcdServiceLoader.getInstance(ILcdModelDecoder.class));
        ILcdModel model7RC = decoderKml.decode("kml/7R.C.M._7R.C.M.kmz");
        ILspLayer layer7RC = TLspKML22LayerBuilder.newBuilder().model(model7RC).build();
        kmdNode.addLayer(layer7RC);

        ILcdModel modelZAP = decoderKml.decode("kml/ZAP_ZAP.KMZ");
        ILspLayer layerZAP = TLspKML22LayerBuilder.newBuilder().model(modelZAP).build();
        kmdNode.addLayer(layerZAP);

        vista.addLayer(kmdNode);

    }

    private void cargaTracksOlin() {
        //Obtiene el mapa
        // Crear modelo vectorial y descriptor
        TLcdGeodeticReference reference = new TLcdGeodeticReference();
        TLcdModelDescriptor descriptor = new TLcdModelDescriptor("WebSocket Tracks", "TRACKS", "Tracks Model");
        TLcdVectorModel model = new TLcdVectorModel(reference, descriptor);

        // Crear el icono 2D y estilo
        //TLcdImageIcon arrowIcon = new TLcdImageIcon("OBJ/vuelo.png"); // Asegúrate de tener el archivo en el directorio correcto
        TLcdSVGIcon iconSVG = new TLcdSVGIcon("svg/plane.svg");
        iconSVG.setSize(30, 30);
        iconSVG.setColor(Color.blue);
        //Point anchorPoint = new Point(arrowIcon.getIconWidth() , arrowIcon.getIconHeight());
        //TLcdAnchoredIcon anchoredIcon = new TLcdAnchoredIcon(arrowIcon, anchorPoint);
        //
        TLspIconStyle iconStyle2D = TLspIconStyle.newBuilder().icon(iconSVG).useOrientation(true)
                //.scale(0.5) // Ajusta el tamaño según tus necesidades
                .scalingMode(TLspIconStyle.ScalingMode.VIEW_SCALING).build();

        // Crear el styler para el icono 2D
        ALspStyler styler2D = new ALspStyler() {
            @Override
            public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
                for (Object obj : aObjects) {
                    if (obj instanceof Track) {
                        aStyleCollector.object(obj).style(iconStyle2D).submit();
                    }
                }
            }
        };

        // Crear capa para el icono 2D y añadir al mapa
        ILspLayer layer2D = TLspShapeLayerBuilder.newBuilder().model(model).bodyStyler(TLspPaintState.REGULAR, styler2D).layerType(ILspLayer.LayerType.REALTIME).build();
        vista.addLayer(layer2D);

        // Crear el icono 3D y estilo
        String modelPath = "OBJ/AirplaneForFreeobj.obj"; // Ruta al modelo 3D
        TLsp3DIconStyle iconStyle3D;
        try {
            iconStyle3D = TLsp3DIconStyle.newBuilder().icon(modelPath).opacity(1.0f).modulationColor(Color.red).scale(2.0) // Ajusta la escala según sea necesario
                    .iconSizeMode(TLsp3DIconStyle.ScalingMode.SCALE_FACTOR).rotate(90, 180, 0) // Ajusta la rotación si es necesario
                    .elevationMode(ILspWorldElevationStyle.ElevationMode.OBJECT_DEPENDENT).translate(0, 8, 0).build();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }

        // Crear el styler para el icono 3D
        ALspStyler styler3D = new ALspStyler() {
            @Override
            public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
                for (Object obj : aObjects) {
                    if (obj instanceof Track) {
                        aStyleCollector.object(obj).style(iconStyle3D).submit();
                    }
                }
            }
        };

        // Crear capa para el icono 3D y añadir al mapa
        ILspLayer layer3D = TLspShapeLayerBuilder.newBuilder().model(model).bodyStyler(TLspPaintState.REGULAR, styler3D).layerType(ILspLayer.LayerType.REALTIME).build();
        vista.addLayer(layer3D);

        // Conectar al WebSocket en un nuevo hilo
        new Thread(() -> connectAndListen(model)).start();

        // Configurar el temporizador para procesar el búfer cada 2 segundos
        Timer bufferTimer = new Timer();
        bufferTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                processBufferedMessages(model);
            }
        }, 0, 2000);// Ejecutar cada 2 segundos
    }


    private void connectAndListen(TLcdVectorModel model) {
        //Conexion a websocket
        List<Transport> transports = new ArrayList<>();
        transports.add(new WebSocketTransport(new StandardWebSocketClient()));//// Agregar transporte WebSocket a la lista

        //Configuración de SockJS que conecta a un WebSocket
        SockJsClient sockJsClient = new SockJsClient(transports);
        //STOMP
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        // Configurar un convertidor de mensajes para interpretar los datos recibidos como JSON
        stompClient.setMessageConverter(new org.springframework.messaging.converter.MappingJackson2MessageConverter());
        stompClient.setTaskScheduler(new ConcurrentTaskScheduler());

        String urlWithToken = URL + "?token=" + TOKEN;
        //conectar al websocket
        stompClient.connect(urlWithToken, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                session.subscribe("/track/output", new StompFrameHandler() {
                    //define el tipo de datos
                    @Override
                    public Type getPayloadType(StompHeaders headers) {
                        return Object.class;
                    }

                    @Override
                    public void handleFrame(StompHeaders headers, Object payload) {
                        // Convertir el mensaje recibido a un String
                        byte[] bytePayload = (byte[]) payload;
                        String message = new String(bytePayload, StandardCharsets.UTF_8);
                        // Agregar el mensaje al búfer
                        updateTrackModel(model, message);
                    }
                });
            }
        });
    }

    private void updateTrackModel(TLcdVectorModel model, String message) {
        // Agregar el mensaje al búfer
        messageBuffer.add(message);
    }


    private void processBufferedMessages(TLcdVectorModel model) {
        List<String> messagesToProcess = new ArrayList<>();
        String message;
        while ((message = messageBuffer.poll()) != null) {
            messagesToProcess.add(message);
        }

        for (String msg : messagesToProcess) {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                // Parsear el mensaje JSON a un Map
                Map<String, Object> trackMessage = objectMapper.readValue(msg, Map.class);

                String flightId = (String) trackMessage.get("fli");
                if (flightId == null || flightId.isEmpty()) {
                    continue; // Saltar si no hay ID de vuelo
                }

                String lonStr = (String) trackMessage.get("lon");
                String latStr = (String) trackMessage.get("lat");
                String altStr = (String) trackMessage.get("alt");
                String heaStr = (String) trackMessage.get("hea");

                if (lonStr == null || latStr == null || altStr == null) {
                    continue; // Saltar si faltan coordenadas
                }

                double lon = Double.parseDouble(lonStr);
                double lat = Double.parseDouble(latStr);
                double alt = Double.parseDouble(altStr);
                double hea = heaStr != null ? Double.parseDouble(heaStr) : 0.0;

                // Crear el objeto Track con orientación
                Track trackPoint = new Track(lon, lat, alt, hea);

                // Actualizar el modelo en el hilo de despacho de eventos (EDT)
                SwingUtilities.invokeLater(() -> {
                    if (trackData.containsKey(flightId)) {
                        // Actualizar solo si la posición o el heading han cambiado
                        Track existingTrack = trackData.get(flightId);
                        if (existingTrack.getX() != lon || existingTrack.getY() != lat || existingTrack.getZ() != alt || existingTrack.getOrientation() != hea) {
                            model.removeElement(existingTrack, ILcdModel.FIRE_NOW);
                            trackData.put(flightId, trackPoint);
                            model.addElement(trackPoint, ILcdModel.FIRE_NOW);
                        }
                    } else {
                        trackData.put(flightId, trackPoint);
                        model.addElement(trackPoint, ILcdModel.FIRE_NOW);
                    }

                    // Gestionar el temporizador para eliminar tracks inactivos
                    if (trackTimers.containsKey(flightId)) {
                        trackTimers.get(flightId).cancel();
                    }

                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            SwingUtilities.invokeLater(() -> {
                                model.removeElement(trackPoint, ILcdModel.NO_EVENT);
                                trackData.remove(flightId);
                                trackTimers.remove(flightId);
                            });
                        }
                    }, 10000); // Eliminar después de 10 segundos

                    trackTimers.put(flightId, timer);
                });

            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
    }

    void agregaFlighradar() throws IOException, InterruptedException {
        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder().uri(new URI("https://fr24api.flightradar24.com/api/live/flight-positions/light?bounds=50.682,46.218,14.422,22.243")).header("Accept", "application/json").header("Accept-Version", "v1").header("Authorization", "Bearer 9d606ec7-d71f-46c2-a835-b6f5b01443e8|xw9ilcjDkmS4rCvYvKQHPUzTpuMVlSNDeofxlhKe7a63c2ae").GET().build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println(response.body());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * Agrega capa desde el servicio WFS de Geoserver
     * Entidades.
     */
    private void agregaWFS() throws URISyntaxException, IOException {
//        String serverURL = "http://localhost:8090/geoserver/shapfiles/ows";
        String serverURL = "http://localhost:8080/albergues";//levantar la api con mvn spring-boot:run
        TLcdWFSClient wfsClient = TLcdWFSClient.createWFSClient(new URI(serverURL));
        ILcdModelDecoder wfsDecoder = new TLcdCompositeModelDecoder(TLcdServiceLoader.getInstance(ILcdModelDecoder.class));

        // Hacemos consulta al WFS GetCapabilities y dependiendo de las capas, cargamos cada una de las capas.
        TLcdWFSCapabilities capabilities = wfsClient.getCachedCapabilities();
        ArrayList<QName> availableFeatureTypes = new ArrayList<>();
        TLcdWFSFeatureTypeList featureTypeList = capabilities.getFeatureTypeList();
        for (int i = 0; i < featureTypeList.getFeatureTypeCount(); i++) {
            TLcdWFSFeatureType featureType = featureTypeList.getFeatureType(i);
            availableFeatureTypes.add(featureType.getName());
        }

        //Generamos los estilos que tendrán nuestros polígonos del WFS.
        TLspFillStyle wfsFillStyleRegular = TLspFillStyle.newBuilder().color(new Color(96, 192, 71, 60)).build();

        TLspLineStyle wfsLineStyleRegular = TLspLineStyle.newBuilder().color(Color.black).width(3).build();

        TLspFillStyle wfsFillStyleSelected = TLspFillStyle.newBuilder().color(new Color(190, 232, 180, 50)).build();

        TLspLineStyle wfsLineStyleSelected = TLspLineStyle.newBuilder().color(Color.white).width(5).build();

        ALspStyle contextoStyle = TLspDataObjectLabelTextProviderStyle.newBuilder().expressions("NOMGEO").build();

        ALspStyle wfsLabelStyleRegular = TLspTextStyle.newBuilder().font(Font.decode("Arial-PLAIN-20")).textColor(Color.white).build();

        ALspStyle wfsLabelStyleSelected = TLspTextStyle.newBuilder().font(Font.decode("Arial-BOLD-12")).textColor(Color.white).build();

        // Por cada una de las capas disponibles, Decodificamos, creamos modelo, creamos layer y cargamos en vista.
        for (int i = 0; i < availableFeatureTypes.size(); i++) {
            TLcdWFSDataSource wfsDataSource = TLcdWFSDataSource.newBuilder().uri(serverURL).featureTypeName(availableFeatureTypes.get(i)).build();
            ILcdModel wfsModel = wfsDecoder.decodeSource(wfsDataSource);
            ILspLayer wfsLayer = TLspShapeLayerBuilder.newBuilder().model(wfsModel).bodyStyles(TLspPaintState.REGULAR, wfsFillStyleRegular, wfsLineStyleRegular).bodyStyles(TLspPaintState.SELECTED, wfsFillStyleSelected, wfsLineStyleSelected).labelStyles(TLspPaintState.REGULAR, contextoStyle, wfsLabelStyleRegular).labelStyles(TLspPaintState.SELECTED, contextoStyle, wfsLabelStyleSelected).build();
            vista.addLayer(wfsLayer);
        }
    }


    private void getCapas() {
        List<ILspLayer> layers = vista.getLayers();
        ILcdLayerTreeNode rootNode = vista.getRootNode();
        //System.out.println(rootNode.);

    }

    private void agregaGridLayer() {
        ILspLayer gridLayer = TLspLonLatGridLayerBuilder.newBuilder().build();
        vista.addLayer(gridLayer);
    }


    /**
     * Crea capa utilizando un Decoder personalizado para conectarse a Servicio Rest
     * @throws URISyntaxException
     * @throws IOException
     * @throws InterruptedException
     */
    private void agregaAlberguesLayer() throws URISyntaxException, IOException, InterruptedException {
        AlbergueModelDecoder decoder = new AlbergueModelDecoder();
//        ILcdModel modelo = decoder.decode("http://localhost:8080/albergues-0.0.1-SNAPSHOT/albergues");
        ILcdModel modelo = decoder.decode("http://localhost:8080/albergues");
        ILspLayer layer = TLspShapeLayerBuilder.newBuilder().model(modelo).build();
        vista.addLayer(layer);
    }

    /**
     * Concentra creación y carga de archivos Shapes
     */
    private void agregaShapeData() {
        try {
            TLspLayerTreeNode shpLayerNode = new TLspLayerTreeNode("Archivos Shapes");

            ILcdModel aeropuertosModel = shapesModel("Shapes/Aerodromos/Aerodromos.shp");
            ILspLayer aeropuertosLayer = aeroPuertoLayer(aeropuertosModel);
            shpLayerNode.addLayer(aeropuertosLayer);

            ILcdModel estadosModel = shapesModel("Shapes/Estados/dest23gw.shp");
            ILspLayer estadosLayer = estadosLayer(estadosModel);
            shpLayerNode.addLayer(estadosLayer);

            vista.addLayer(shpLayerNode);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Crea Capa de Entidades
     * @param estadosModel
     *
     * @return
     */
    private ILspLayer estadosLayer(ILcdModel estadosModel) {
        TLspFillStyle estadoFillStyleRegular = TLspFillStyle.newBuilder().color(new Color(96, 192, 71, 60)).build();

        TLspLineStyle estadoLineStyleRegular = TLspLineStyle.newBuilder().color(Color.black).width(3).build();

        TLspFillStyle estadoFillStyleSelected = TLspFillStyle.newBuilder().color(new Color(190, 232, 180, 50)).build();

        TLspLineStyle estadoLineStyleSelected = TLspLineStyle.newBuilder().color(Color.white).width(5).build();

        ALspStyle contextoStyle = TLspDataObjectLabelTextProviderStyle.newBuilder().expressions("NOMGEO").build();

        ALspStyle estadoLabelStyleRegular = TLspTextStyle.newBuilder().font(Font.decode("Arial-PLAIN-20")).textColor(Color.white).build();

        ALspStyle estadoLabelStyleSelected = TLspTextStyle.newBuilder().font(Font.decode("Arial-BOLD-12")).textColor(Color.white).build();

        return TLspShapeLayerBuilder.newBuilder().model(estadosModel).bodyStyles(TLspPaintState.REGULAR, estadoFillStyleRegular, estadoLineStyleRegular).bodyStyles(TLspPaintState.SELECTED, estadoFillStyleSelected, estadoLineStyleSelected).labelStyles(TLspPaintState.REGULAR, contextoStyle, estadoLabelStyleRegular).labelStyles(TLspPaintState.SELECTED, contextoStyle, estadoLabelStyleSelected).label("Entidades Federativas").build();
    }

    /**
     * Crea capa de Aeródromos
     * @param aeropuertosModel
     *
     * @return
     */
    private ILspLayer aeroPuertoLayer(ILcdModel aeropuertosModel) {
        ////// configura sstilos a los aerodromos.
        TLcdSVGIcon icon = new TLcdSVGIcon("svg/airport-sign-1-svgrepo-com.svg");
        icon.setSize(20, 20);

        TLcdSVGIcon iconSelected = new TLcdSVGIcon("svg/airport-sign-1-svgrepo-com.svg");
        iconSelected.setColor(Color.white);
        iconSelected.setSize(30, 30);

        TLcdSVGIcon con = (TLcdSVGIcon) iconSelected.clone();
        con.setSize(30, 30);
        con.setColor(Color.cyan);

        TLspIconStyle iconStyle = TLspIconStyle.newBuilder().icon(con).build();
        TLspIconStyle iconSelectedStyle = TLspIconStyle.newBuilder().icon(iconSelected).build();
        ALspStyle labelAerodromo = TLspDataObjectLabelTextProviderStyle.newBuilder().expressions("ICAO").build();
        TLspTextStyle textStyle = TLspTextStyle.newBuilder().textColor(Color.white).font(Font.decode("Arial12")).build();

        ILspLayer aeropuertosLayer = TLspShapeLayerBuilder.newBuilder().bodyStyles(TLspPaintState.REGULAR, iconStyle).bodyStyles(TLspPaintState.SELECTED, iconSelectedStyle).labelStyles(TLspPaintState.REGULAR, labelAerodromo, textStyle).selectable(true).model(aeropuertosModel).label("Aeródromos").build();

        return aeropuertosLayer;
    }


    /**
     * @param fileShp Crea Modelos para archivos Shapes.
     *
     * @return
     * @throws IOException
     */
    private ILcdModel shapesModel(String fileShp) throws IOException {
        ILcdModelDecoder shpModelDecoder = new TLcdSHPModelDecoder();
        ILcdModel model = shpModelDecoder.decode(fileShp);
        return model;
    }


    /**
     * Agrega Mapa Base
     */
    private void agregaGeotiffData() {
        ILcdModelDecoder decoder = new TLcdGeoTIFFModelDecoder();
        try {
            try (InputStream inputStream = getClass().getResourceAsStream("/home/vboxuser/Escritorio/appBasica/src/resources/BlueMarble/bluemarble.tif")) {
                assert inputStream != null;
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String contents = reader.lines()
                            .collect(Collectors.joining(System.lineSeparator()));
                    ILcdModelDecoder decoderr = new TLcdCompositeModelDecoder(TLcdServiceLoader.getInstance(ILcdModelDecoder.class));
                    ILcdModel modelo = decoderr.decode("BlueMarble/bluemarble.tif");
                    ILspLayer layer = new TLspRasterLayer(modelo);
                    vista.addLayer(layer);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Cambia vista a 2D
     * @return
     */
    static Action createSwitchTo2DAction() {
        AbstractAction action = new AbstractAction("2D") {
            @Override
            public void actionPerformed(ActionEvent e) {
                TLspViewTransformationUtil.setup2DView(vista, new TLcdGridReference(new TLcdGeodeticDatum(), new TLcdEquidistantCylindrical()), true);
            }
        };
        action.putValue(Action.SHORT_DESCRIPTION, "Cambia a 2D");
        return action;
    }


    /**
     * Cambia a 3D
     * @return
     */
    private Action createSwitchTo3DAction() {
        AbstractAction action = new AbstractAction("3D") {
            @Override
            public void actionPerformed(ActionEvent e) {
                TLspViewTransformationUtil.setup3DView(vista, true);
            }
        };
        action.putValue(Action.SHORT_DESCRIPTION, "Cambia a 3D");
        return action;
    }

    public final ILcdList<ILcdLayer> getSelectedLayers() {
        return fSelectedLayers;
    }

    private void agregaFlightRadar(){
        // Crear modelo vectorial y descriptor
        TLcdGeodeticReference reference = new TLcdGeodeticReference();
        TLcdModelDescriptor descriptor = new TLcdModelDescriptor("Flight Tracks", "TRACKS", "Tracks Model");
        TLcdVectorModel model = new TLcdVectorModel(reference, descriptor);

        // Crear el icono 2D y estilo
        TLcdSVGIcon arrowIcon = new TLcdSVGIcon("svg/plane.svg");
        arrowIcon.setSize(30,30);
        arrowIcon.setColor(Color.green);
        Point anchorPoint = new Point(arrowIcon.getIconWidth() / 2, arrowIcon.getIconHeight());
        TLcdAnchoredIcon anchoredIcon = new TLcdAnchoredIcon(arrowIcon, anchorPoint);

        TLspIconStyle iconStyle2D = TLspIconStyle.newBuilder()
                .icon(arrowIcon)
                .useOrientation(true)
                .scale(0.05)
                .build();

        // Crear el styler para el icono 2D
        ALspStyler styler2D = new ALspStyler() {
            @Override
            public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
                for (Object obj : aObjects) {
                    if (obj instanceof Track) {
                        aStyleCollector.object(obj)
                                .style(iconStyle2D)
                                .submit();
                    }
                }
            }
        };

        // Crear la capa para el icono 2D para añadir al mapa posteriormente
        ILspLayer layer2D = TLspShapeLayerBuilder.newBuilder()
                .model(model)
                .bodyStyler(TLspPaintState.REGULAR, styler2D)
                .layerType(ILspLayer.LayerType.REALTIME)
                .label("flightradar")
                .build();
        vista.addLayer(layer2D);

        // Crear el icono 3D y estilo (puede ser formato .obj, .dae o .flt)
        String modelPath = "OBJ/AirplaneForFreeobj.obj";
        TLsp3DIconStyle iconStyle3D;
        try {
            iconStyle3D = TLsp3DIconStyle.newBuilder()
                    .icon(modelPath)
                    .opacity(1.0f)
                    .modulationColor(Color.red)
                    .scale(2.0)
                    .iconSizeMode(TLsp3DIconStyle.ScalingMode.SCALE_FACTOR)
                    .rotate(90, 180, 0)
                    .elevationMode(ILspWorldElevationStyle.ElevationMode.OBJECT_DEPENDENT)
                    .translate(0, 8, 0)
                    .build();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return;
        }

        // Crear el styler para el icono 3D
        ALspStyler styler3D = new ALspStyler() {
            @Override
            public void style(Collection<?> aObjects, ALspStyleCollector aStyleCollector, TLspContext aContext) {
                for (Object obj : aObjects) {
                    if (obj instanceof Track) {
                        aStyleCollector.object(obj)
                                .style(iconStyle3D)
                                .submit();
                    }
                }
            }
        };

        // Crear capa para el icono 3D para añadir al mapa posteriormente
        ILspLayer layer3D = TLspShapeLayerBuilder.newBuilder()
                .model(model)
                .bodyStyler(TLspPaintState.REGULAR, styler3D)
                .layerType(ILspLayer.LayerType.REALTIME)
                .label("flightradar 3d")
                .build();
        vista.addLayer(layer3D);

        // Inicia las peticiones cada 6 segundos para evitar bloqueo de api
        startScheduledTask(model);
    }

    private void startScheduledTask(TLcdVectorModel model) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                // Realizar la petición HTTP
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI("https://fr24api.flightradar24.com/api/live/flight-positions/light?bounds=19.6,19.2,-99.4,-98.9"))
                        .header("Accept", "application/json")
                        .header("Accept-Version", "v1")
                        .header("Authorization", "Bearer 9d6160c7-2c77-409e-ab9b-8b97b46c9461|KxfNXjgEqvQFeSEV3Pz5LMLCjH78j5dAOuNngfdt0ba71aa8")
                        .GET()
                        .build();

                HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println(response.body());

                // ACTUALIZAR EL MODELO CON LOS NUEVOS DATOS DE LA PETICION
                updateTrackModel(model, response.body());

            } catch (URISyntaxException | InterruptedException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 6, TimeUnit.SECONDS);
    }


    private void simbologiaMilitar() {
        TLcdVectorModel milsim = new TLcdVectorModel(new TLcdGeodeticReference());
        milsim.setModelDescriptor(new TLcdMS2525bModelDescriptor(null, "MIL", "MS2525", null));

        TLcdEditableMS2525bObject object =
                new TLcdEditableMS2525bObject("S*A*MFKD--*****", ELcdMS2525Standard.MIL_STD_2525b);
        object.move2DPoint(0, -97.768880, 21.375393); // Move the object
        object.putTextModifier(ILcdMS2525bCoded.sUniqueDesignation, "Regular");
        milsim.addElement(object, ILcdFireEventMode.NO_EVENT);

        ILspLayer milsym = TLspMS2525bLayerBuilder.newBuilder()
                .model(milsim)
                .label("Simbología")
                .build();

        vista.addLayer(milsym);
    }
}
