package com.example.mapboxdemo.view.fragment

import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.example.mapboxdemo.R
import com.example.mapboxdemo.data.model.CurrentWeatherInfo
import com.example.mapboxdemo.databinding.FragmentMapBinding
import com.example.mapboxdemo.databinding.LayoutPopupBinding
import com.example.mapboxdemo.utils.CoverageMap
import com.example.mapboxdemo.viewmodel.MainViewModel
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point
import com.mapbox.geojson.Point.fromLngLat
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.*
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.style.sources.RasterSource
import com.mapbox.mapboxsdk.style.sources.TileSet


class MapFragment : Fragment(), OnMapReadyCallback, MapboxMap.OnMapClickListener {

    private lateinit var binding: FragmentMapBinding
    private val viewModel: MainViewModel by viewModels()
    lateinit var mapBox: MapboxMap
    var style: Style? = null
    private var routeCoordinates = ArrayList<Point>()
    private lateinit var departureData: CurrentWeatherInfo
    private lateinit var destinationData: CurrentWeatherInfo
    private lateinit var popUpBinding: LayoutPopupBinding
    val POPUP_LAYER_ID = "mapBoxMap.popup"
    lateinit var coverageImage: String
    /* private val ID_ICON_START = "start-position"
     private val ID_ICON_END = "end-position"
     private var symbol: Symbol? = null*/

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Mapbox.getInstance(requireActivity(), getString(R.string.mapbox_access_token))
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_map, container, false)
        popUpBinding = DataBindingUtil.inflate(inflater, R.layout.layout_popup, container, false)
        setupViewModel()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapView.getMapAsync(this)

    }

    override fun onMapReady(mapboxMap: MapboxMap) {

        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            style = it
            this.mapBox = mapboxMap
            mapBox.addOnMapClickListener(this)

            val initialLatLng = LatLng(23.846617, 90.406392)
            val cameraPosition =
                CameraPosition.Builder().target(initialLatLng).zoom(5.0).tilt(15.0).build()
            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            drawPopupLayer(it, ArrayList(), popUpBinding)
            markStartPont(it, ArrayList())
            markDestinationPoint(it, ArrayList())
            /* image1ToStyle(it)
             image2ToStyle(it)*/
            initRouterCoordinates()
            drawLineLayer(it, routeCoordinates)
            showLineLayer(it)

            CoverageMap.values().forEach { values ->
                coverageImage = ""
                if (values.layerName == CoverageMap.FLEXXEC.layerName) {
                    coverageImage = getString(R.string.flexexec_image)
                }
                if (values.layerName == CoverageMap.VIASAT_KU.layerName) {
                    coverageImage = getString(R.string.viasat_ku_image)
                }
                drawOverlayImageLayer(style,values.layerName, coverageImage)
            }

            binding.departureCard.setOnClickListener {
                val latLng = LatLng(23.846617, 90.406392)
                val position = CameraPosition.Builder().target(latLng).zoom(16.0).tilt(25.0).build()
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position))
            }

            binding.destinationCard.setOnClickListener {
                val latLng = LatLng(22.247928, 91.814863)
                val position = CameraPosition.Builder().target(latLng).zoom(14.0).tilt(25.0).build()
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position))
            }

            /*val symbolManager = SymbolManager(binding.mapView, mapboxMap, it)
            symbolManager.addClickListener { symbol ->
                Log.d(TAG, "onMapReady: Symbol clicked")
                false
            }
            symbolManager.addClickListener { symbol ->
                Log.d(TAG, "onMapReady: Symbol clicked")
                false
            }
            symbolManager.iconAllowOverlap = true
            symbolManager.iconTranslate = arrayOf(-4f, 5f)
            symbolManager.iconRotationAlignment = ICON_ROTATION_ALIGNMENT_VIEWPORT
            symbolManager.iconAllowOverlap = true
            symbolManager.iconIgnorePlacement = true

            val slatLng = LatLng(23.8223, 90.3654)
            val symbolOptions = SymbolOptions()
                .withLatLng(slatLng)
                .withIconImage(ID_ICON_START)
                .withIconColor(ColorUtils.colorToRgbaString(Color.BLUE))
                .withIconSize(.7f)
                .withSymbolSortKey(10.0f)
                .withDraggable(false)
            symbol = symbolManager.create(symbolOptions)

            val eLatLng = LatLng(6.687337, 0.381457)
            val nearbyOptions = SymbolOptions()
                .withLatLng(eLatLng)
                .withIconImage(ID_ICON_END)
                .withIconColor(ColorUtils.colorToRgbaString(Color.GREEN))
                .withIconSize(.7f)
                .withSymbolSortKey(10.0f)
            symbol = symbolManager.create(nearbyOptions)*/
        }
    }

    override fun onMapClick(point: LatLng): Boolean {
        if (style?.isFullyLoaded == false) {
            return false
        }
        val screenPoint = mapBox.projection.toScreenLocation(point)
        Log.d("TAG", "outside: $screenPoint")
        val popupLayout = mapBox.queryRenderedFeatures(screenPoint, POPUP_LAYER_ID)
        if (popupLayout.isNotEmpty()) {
            val symbolScreenPoint =
                mapBox.projection.toScreenLocation(convertToLatLng(popupLayout[0]))
            handlePopupClick(screenPoint, symbolScreenPoint)
            Log.d("TAG", "inside: $symbolScreenPoint")
        }

        val startMarkerFeatures = mapBox.queryRenderedFeatures(screenPoint, "StartPointMarker")
        if (startMarkerFeatures.isNotEmpty()) {
            showPopupLayer(style)
        }
        return true
    }

    private fun handlePopupClick(screenPoint: PointF, symbolScreenPoint: PointF) {
        val hitRecCross = Rect()
        popUpBinding.popupClose.getHitRect(hitRecCross)
        val fX = convertDpToPx(this, 100f).toInt()
        val fY = convertDpToPx(this, 110f).toInt()
        hitRecCross.offset(symbolScreenPoint.x.toInt() - fX, symbolScreenPoint.y.toInt() - fY)
        if (hitRecCross.contains(screenPoint.x.toInt(), screenPoint.y.toInt())) {
            hidePopupLayer(style)
        }

        val hitRecFlexExec = Rect()
        popUpBinding.changeToFlexXec.getHitRect(hitRecFlexExec)
        val fX1 = convertDpToPx(this, 100f).toInt()
        val fY1 = convertDpToPx(this, 110f).toInt()
        hitRecFlexExec.offset(symbolScreenPoint.x.toInt() - fX1, symbolScreenPoint.y.toInt() - fY1)
        if (hitRecFlexExec.contains(screenPoint.x.toInt(), screenPoint.y.toInt())) {
            showOverlayImageLayer(style,CoverageMap.FLEXXEC.layerName)
            hideOverLayImageLayer(style,CoverageMap.VIASAT_KU.layerName)
        }

        val hitRecViasatKu = Rect()
        popUpBinding.changeToViSatKu.getHitRect(hitRecViasatKu)
        Log.d("Rony2", " something hitRecViasatKu 1 -> ${hitRecViasatKu}")
        val fX2 = convertDpToPx(this, 100f).toInt()
        val fY2 = convertDpToPx(this, 110f).toInt()
        hitRecViasatKu.offset(symbolScreenPoint.x.toInt() - fX2, symbolScreenPoint.y.toInt() - fY2)
        Log.d("Rony2", " something hitRecViasatKu 2 -> ${hitRecViasatKu}")
        Log.d("Rony2", " something fX -> ${fX2}")
        Log.d("Rony2", " something fY -> ${fY2}")
        Log.d("Rony2", "something symbolScreenPoint.x.toInt() -> ${symbolScreenPoint.x.toInt()}")
        Log.d("Rony2", "something symbolScreenPoint.y.toInt() -> ${symbolScreenPoint.y.toInt()}")
        Log.d(
            "Rony2",
            "something symbolScreenPoint.x.toInt()-ooX -> ${symbolScreenPoint.x.toInt() - fX2}"
        )
        Log.d(
            "Rony2",
            " something symbolScreenPoint.y.toInt()-ooY -> ${symbolScreenPoint.y.toInt() - fY2}"
        )
        if (hitRecViasatKu.contains(screenPoint.x.toInt(), screenPoint.y.toInt())) {
            showOverlayImageLayer(style,CoverageMap.VIASAT_KU.layerName)
            hideOverLayImageLayer(style,CoverageMap.FLEXXEC.layerName)
            Log.d("Rony2", "something if screenPoint.x.toInt() -> ${screenPoint.x.toInt()}")
            Log.d("Rony2", "something if screenPoint.x.toInt() -> ${screenPoint.y.toInt()}")
        }else {
            Log.d("Rony2", "something if screenPoint.x.toInt() -> ${screenPoint.x.toInt()}")
            Log.d("Rony2", "something if screenPoint.x.toInt() -> ${screenPoint.y.toInt()}")
        }

        val hitRecNormalMap = Rect()
        popUpBinding.changeToNormal.getHitRect(hitRecNormalMap)
        val fX3 = convertDpToPx(this, 100f).toInt()
        val fY3 = convertDpToPx(this, 110f).toInt()
        hitRecNormalMap.offset(symbolScreenPoint.x.toInt() - fX3, symbolScreenPoint.y.toInt() - fY3)
        if (hitRecNormalMap.contains(screenPoint.x.toInt(), screenPoint.y.toInt())) {
            hideOverLayImageLayer(style,CoverageMap.VIASAT_KU.layerName)
            hideOverLayImageLayer(style,CoverageMap.FLEXXEC.layerName)

        }
    }

    private fun convertToLatLng(feature: Feature): LatLng {
        val symbolPoint = feature.geometry() as Point?
        return LatLng(symbolPoint!!.latitude(), symbolPoint.longitude())
    }

    private fun convertDpToPx(context: MapFragment, dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }

    /* private fun image1ToStyle(style: Style) {
         style.addImage(
             ID_ICON_START,
             BitmapUtils.getBitmapFromDrawable(resources.getDrawable(R.drawable.mapbox_marker_icon_green))!!,
             true
         )
     }

     private fun image2ToStyle(style: Style) {
         style.addImage(
             ID_ICON_END,
             BitmapUtils.getBitmapFromDrawable(resources.getDrawable(R.drawable.mapbox_marker_blue))!!,
             true
         )
     }*/

    private fun setupViewModel() {
        viewModel.getDepartureWeatherInfo()
        viewModel._departureWeatherInfo.observe(viewLifecycleOwner, {
            departureData = it
            Log.d("TAG", "setupViewModel: ${departureData}")

            binding.departure = departureData
            Glide.with(this)
                .load(("https:" + departureData.current.condition.icon))
                .into(binding.departureWeatherImage)
        })

        viewModel.getDestinationWeatherInfo()
        viewModel._destinationWeatherInfo.observe(viewLifecycleOwner, {
            destinationData = it
            binding.destination = destinationData
            Glide.with(this)
                .load(("https:" + destinationData.current.condition.icon))
                .into(binding.destinationWeatherImage)
        })
    }

    private fun markStartPont(
        style: Style?,
        startPoints: ArrayList<Feature>
    ) {
        val startLong = 90.406392
        val startLat = 23.846617

        startPoints.add(
            (Feature.fromGeometry(
                fromLngLat(
                    startLong, startLat
                )
            ))
        )
        style?.addSource(
            GeoJsonSource(
                "StartMarker_source",
                FeatureCollection.fromFeatures(startPoints)
            )
        )
        style?.addImage(
            "StartMarker_source",
            BitmapFactory.decodeResource(context?.resources, R.drawable.mapbox_marker_icon_green)
        )
        style?.addLayer(
            SymbolLayer("StartPointMarker", "StartMarker_source").withProperties(
                PropertyFactory.iconImage("StartMarker_source"),
                PropertyFactory.iconSize(.5f),
                PropertyFactory.iconAllowOverlap(true)
            )
        )
    }

    private fun markDestinationPoint(
        style: Style?,
        endPoints: ArrayList<Feature>
    ) {
        val endLong = 91.814863
        val endLat = 22.247928

        endPoints.add(
            (Feature.fromGeometry(
                fromLngLat(
                    endLong, endLat
                )
            ))
        )

        style?.addSource(
            GeoJsonSource(
                "endPoint_marker_source",
                FeatureCollection.fromFeatures(endPoints)
            )
        )

        style?.addImage(
            "endPoint_marker_source",
            BitmapFactory.decodeResource(context?.resources, R.drawable.mapbox_marker_icon_red)
        )

        style?.addLayer(
            SymbolLayer("endpoint_marker", "endPoint_marker_source").withProperties(
                PropertyFactory.iconImage("endPoint_marker_source"),
                PropertyFactory.iconSize(.5f),
                PropertyFactory.iconAllowOverlap(true)
            )
        )
    }

    private fun drawLineLayer(style: Style?, routeCoordinates: ArrayList<Point>) {
        style?.addSource(
            GeoJsonSource(
                "LineLayer-source",
                FeatureCollection.fromFeatures(
                    arrayOf(
                        Feature.fromGeometry(
                            LineString.fromLngLats(routeCoordinates)
                        )
                    )
                )
            )
        )

        style?.addLayer(
            LineLayer("LineLayer", "LineLayer-source").withProperties(
                PropertyFactory.visibility(Property.NONE),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(2f),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.lineColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.mapbox_blue
                    )
                )
            )
        )
    }

    private fun showLineLayer(style: Style?) {
        val layer = style?.getLayer("LineLayer")
        layer?.setProperties(PropertyFactory.visibility(Property.VISIBLE))
    }

    private fun drawPopupLayer(
        style: Style?,
        markerPoint: ArrayList<Feature>,
        popupBinding: LayoutPopupBinding
    ) {
        val startLong = 90.406392
        val startLat = 23.846617

        markerPoint.add(
            (Feature.fromGeometry(
                fromLngLat(
                    startLong, startLat
                )
            ))
        )

        style?.addSource(
            GeoJsonSource(
                "marker_source", FeatureCollection.fromFeatures(markerPoint)
            )
        )

        val bitmap = bitmapBuilder(popupBinding.root)

        style?.addImage("marker_source", bitmap)

        style?.addLayer(
            SymbolLayer(POPUP_LAYER_ID, "marker_source").withProperties(
                PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),
                PropertyFactory.iconOffset(arrayOf(-50f, -15f)),
                PropertyFactory.visibility(Property.NONE),
                PropertyFactory.iconImage("marker_source"),
                PropertyFactory.iconAllowOverlap(true)
            )
        )
    }

    private fun showPopupLayer(style: Style?) {
        val layer = style?.getLayer(POPUP_LAYER_ID)
        layer?.setProperties(PropertyFactory.visibility(Property.VISIBLE))
    }

    private fun hidePopupLayer(style: Style?) {
        val layer = style?.getLayer(POPUP_LAYER_ID)
        layer?.setProperties(PropertyFactory.visibility(Property.NONE))
    }

    private fun drawOverlayImageLayer(style: Style?, layer: String, coverageMaps: String) {
        val tileSet = TileSet("tileset", "asset://$coverageMaps")
        tileSet.minZoom = 0f
        tileSet.maxZoom = 0f
        style?.addSource(RasterSource(layer, tileSet, 512))
        style?.addLayer(
            RasterLayer(layer, layer).withProperties(
                PropertyFactory.visibility(Property.NONE)
            ))
    }

    private fun showOverlayImageLayer(style: Style?, layer: String) {
        val currentLayer = style?.getLayer(layer)
        currentLayer?.setProperties(PropertyFactory.visibility(Property.VISIBLE))
    }

    private fun hideOverLayImageLayer(style: Style?, layer: String) {
        val currentLayer = style?.getLayer(layer)
        if (currentLayer != null && currentLayer.visibility.value == Property.VISIBLE) {
            currentLayer?.setProperties(PropertyFactory.visibility(Property.NONE))
        }
    }


    private fun initRouterCoordinates() {
        /*(routeCoordinates as ArrayList<Point>).add(Point.fromLngLat(90.3654,23.8223 ))
        (routeCoordinates as ArrayList<Point>).add(Point.fromLngLat(0.381457, 6.687337))*/
        routeCoordinates.add(fromLngLat(90.406392, 23.846617))
        routeCoordinates.add(fromLngLat(90.250630, 23.500450))
        routeCoordinates.add(fromLngLat(90.397526, 23.701323))
        routeCoordinates.add(fromLngLat(90.4841, 23.6895))
        routeCoordinates.add(fromLngLat(90.5217, 23.7057))
        routeCoordinates.add(fromLngLat(90.6073, 23.6214))
        routeCoordinates.add(fromLngLat(90.7669, 23.5518))
        routeCoordinates.add(fromLngLat(91.1809, 23.4607))
        routeCoordinates.add(fromLngLat(91.3976, 23.0159))
        routeCoordinates.add(fromLngLat(91.5742, 22.7730))
        routeCoordinates.add(fromLngLat(91.6809, 22.6171))
        routeCoordinates.add(fromLngLat(91.4075, 22.5085))
        routeCoordinates.add(fromLngLat(91.814863, 22.247928))
    }

    private fun bitmapBuilder(view: View): Bitmap {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(measureSpec, measureSpec)
        val measureWidth = view.measuredWidth
        val measureHeight = view.measuredHeight
        view.layout(0, 0, measureWidth, measureHeight)
        val bitmap = Bitmap.createBitmap(measureWidth, measureHeight, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(Color.TRANSPARENT)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }
}