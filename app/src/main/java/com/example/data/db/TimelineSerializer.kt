package com.example.data.db

import com.example.data.model.AudioCategory
import com.example.data.model.AudioFadeType
import com.example.data.model.AudioTrackItem
import com.example.data.model.ImageOverlay
import com.example.data.model.KeyframeCurve
import com.example.data.model.OpenShotKeyframe
import com.example.data.model.OverlaySticker
import com.example.data.model.ShapeAnimation
import com.example.data.model.ShapeOverlay
import com.example.data.model.ShapeType
import com.example.data.model.StabilizationMode
import com.example.data.model.StabilizationSettings
import com.example.data.model.TextStylePreset
import com.example.data.model.TextOverlay
import com.example.data.model.TransitionType
import com.example.data.model.VideoClip
import com.example.data.model.VideoEffectItem
import com.example.data.model.VideoEffectType
import com.example.data.model.VideoFilter
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

data class TimelineData(
    val clips: List<VideoClip>,
    val audioClips: List<AudioTrackItem>,
    val textOverlays: List<TextOverlay>,
    val stickers: List<OverlaySticker>,
    val shapes: List<ShapeOverlay> = emptyList(),
    val imageOverlays: List<ImageOverlay> = emptyList()
)

object TimelineSerializer {
    fun serialize(data: TimelineData): String {
        val root = JSONObject()

        val clipsArray = JSONArray()
        data.clips.forEach { clip ->
            val obj = JSONObject().apply {
                put("id", clip.id)
                put("name", clip.name)
                put("uri", clip.uri)
                put("sourceDurationMs", clip.sourceDurationMs)
                put("trimInMs", clip.trimInMs)
                put("trimOutMs", clip.trimOutMs)
                put("timelineStartMs", clip.timelineStartMs)
                put("speed", clip.speed.toDouble())
                put("volume", clip.volume.toDouble())
                put("isMuted", clip.isMuted)
                put("filter", clip.filter.name)
                put("filterIntensity", clip.filterIntensity.toDouble())
                put("brightness", clip.brightness.toDouble())
                put("contrast", clip.contrast.toDouble())
                put("saturation", clip.saturation.toDouble())
                put("transitionIn", clip.transitionIn.name)
                put("transitionDurationMs", clip.transitionDurationMs)
                put("scale", clip.scale.toDouble())
                put("rotation", clip.rotation.toDouble())
                put("flipHorizontal", clip.flipHorizontal)
                put("isReversed", clip.isReversed)
                put("isFreezeFrame", clip.isFreezeFrame)
                put("hasKeyframe", clip.hasKeyframe)
                put("keyframeScale", clip.keyframeScale.toDouble())
                put("canvasBgMode", clip.canvasBgMode.name)
                put("accentColorHex", clip.accentColorHex)
                put("isChromaKeyEnabled", clip.isChromaKeyEnabled)
                put("chromaKeyColorHex", clip.chromaKeyColorHex)
                put("chromaKeyTolerance", clip.chromaKeyTolerance.toDouble())
                put("alpha", clip.alpha.toDouble())
                put("positionX", clip.positionX.toDouble())
                put("positionY", clip.positionY.toDouble())
                put("isVertical", clip.isVertical)
                put("canvasBlurRadiusDp", clip.canvasBlurRadiusDp.toDouble())
                put("canvasBgBrightness", clip.canvasBgBrightness.toDouble())
                put("hasSpeech", clip.hasSpeech)
                put("cropLeft", clip.cropLeft.toDouble())
                put("cropTop", clip.cropTop.toDouble())
                put("cropRight", clip.cropRight.toDouble())
                put("cropBottom", clip.cropBottom.toDouble())
                put("resizeWidth", clip.resizeWidth)
                put("resizeHeight", clip.resizeHeight)
                put("fadeInMs", clip.fadeInMs)
                put("fadeOutMs", clip.fadeOutMs)
                put("width", clip.width)
                put("height", clip.height)
                put("frameRate", clip.frameRate.toDouble())
                put("videoCodec", clip.videoCodec)
                put("mimeType", clip.mimeType)
                put("hasAudio", clip.hasAudio)
                put("fileSizeBytes", clip.fileSizeBytes)
                put("thumbnailPath", clip.thumbnailPath)
                // Color Grading
                val cgObj = JSONObject().apply {
                    put("exposure", clip.colorGrading.exposure.toDouble())
                    put("temperature", clip.colorGrading.temperature.toDouble())
                    put("tint", clip.colorGrading.tint.toDouble())
                    put("vibrance", clip.colorGrading.vibrance.toDouble())
                    put("lut", clip.colorGrading.lut.name)
                    put("liftR", clip.colorGrading.liftR.toDouble())
                    put("liftG", clip.colorGrading.liftG.toDouble())
                    put("liftB", clip.colorGrading.liftB.toDouble())
                    put("gammaR", clip.colorGrading.gammaR.toDouble())
                    put("gammaG", clip.colorGrading.gammaG.toDouble())
                    put("gammaB", clip.colorGrading.gammaB.toDouble())
                    put("gainR", clip.colorGrading.gainR.toDouble())
                    put("gainG", clip.colorGrading.gainG.toDouble())
                    put("gainB", clip.colorGrading.gainB.toDouble())
                    put("liftPedestal", clip.colorGrading.liftPedestal.toDouble())
                    put("gammaPedestal", clip.colorGrading.gammaPedestal.toDouble())
                    put("gainPedestal", clip.colorGrading.gainPedestal.toDouble())
                }
                put("colorGrading", cgObj)
                // OpenShot Effects
                val fxArr = JSONArray()
                clip.effects.forEach { fx ->
                    val fxObj = JSONObject().apply {
                        put("id", fx.id)
                        put("type", fx.type.name)
                        put("intensity", fx.intensity.toDouble())
                        put("secondaryParam", fx.secondaryParam.toDouble())
                        put("isEnabled", fx.isEnabled)
                    }
                    fxArr.put(fxObj)
                }
                put("effects", fxArr)
                // OpenShot Keyframes
                val kfArr = JSONArray()
                clip.keyframes.forEach { kf ->
                    val kfObj = JSONObject().apply {
                        put("id", kf.id)
                        put("timeOffsetMs", kf.timeOffsetMs)
                        put("scale", kf.scale.toDouble())
                        put("rotation", kf.rotation.toDouble())
                        put("alpha", kf.alpha.toDouble())
                        put("positionX", kf.positionX.toDouble())
                        put("positionY", kf.positionY.toDouble())
                        put("curve", kf.curve.name)
                    }
                    kfArr.put(kfObj)
                }
                put("keyframes", kfArr)
                // Hardware Stabilization
                val stabObj = JSONObject().apply {
                    put("isEnabled", clip.stabilization.isEnabled)
                    put("mode", clip.stabilization.mode.name)
                    put("smoothness", clip.stabilization.smoothness.toDouble())
                    put("cropMarginPercent", clip.stabilization.cropMarginPercent.toDouble())
                    put("reduceRollingShutter", clip.stabilization.reduceRollingShutter)
                }
                put("stabilization", stabObj)
            }
            clipsArray.put(obj)
        }
        root.put("clips", clipsArray)

        val audioArray = JSONArray()
        data.audioClips.forEach { audio ->
            val obj = JSONObject().apply {
                put("id", audio.id)
                put("name", audio.name)
                put("uri", audio.uri)
                put("timelineStartMs", audio.timelineStartMs)
                put("durationMs", audio.durationMs)
                put("volume", audio.volume.toDouble())
                put("isMuted", audio.isMuted)
                put("category", audio.category.name)
                put("fadeInMs", audio.fadeInMs)
                put("fadeOutMs", audio.fadeOutMs)
                put("isDuckingEnabled", audio.isDuckingEnabled)
                put("accentColorHex", audio.accentColorHex)
                put("isBeatSyncEnabled", audio.isBeatSyncEnabled)
                put("pan", audio.pan.toDouble())
                put("fadeType", audio.fadeType.name)
                val beatsArr = JSONArray()
                audio.beatMarkersMs.forEach { beatsArr.put(it) }
                put("beatMarkersMs", beatsArr)
            }
            audioArray.put(obj)
        }
        root.put("audioClips", audioArray)

        val textArray = JSONArray()
        data.textOverlays.forEach { text ->
            val obj = JSONObject().apply {
                put("id", text.id)
                put("text", text.text)
                put("timelineStartMs", text.timelineStartMs)
                put("durationMs", text.durationMs)
                put("positionX", text.positionX.toDouble())
                put("positionY", text.positionY.toDouble())
                put("fontSizeSp", text.fontSizeSp)
                put("textColorHex", text.textColorHex)
                put("bgColorHex", text.bgColorHex)
                put("preset", text.preset.name)
                put("subtitle", text.subtitle)
                put("borderWidthDp", text.borderWidthDp.toDouble())
                put("borderColorHex", text.borderColorHex)
                put("is3D", text.is3D)
                put("template", text.template)
            }
            textArray.put(obj)
        }
        root.put("textOverlays", textArray)

        val stickersArray = JSONArray()
        data.stickers.forEach { sticker ->
            val obj = JSONObject().apply {
                put("id", sticker.id)
                put("label", sticker.label)
                put("emojiOrIcon", sticker.emojiOrIcon)
                put("timelineStartMs", sticker.timelineStartMs)
                put("durationMs", sticker.durationMs)
                put("positionX", sticker.positionX.toDouble())
                put("positionY", sticker.positionY.toDouble())
                put("scale", sticker.scale.toDouble())
                put("isAnimated", sticker.isAnimated)
                put("animationType", sticker.animationType)
                put("loopAnimation", sticker.loopAnimation)
            }
            stickersArray.put(obj)
        }
        root.put("stickers", stickersArray)

        val shapesArray = JSONArray()
        data.shapes.forEach { shape ->
            val obj = JSONObject().apply {
                put("id", shape.id)
                put("shapeType", shape.shapeType.name)
                put("fillColorHex", shape.fillColorHex)
                put("strokeColorHex", shape.strokeColorHex)
                put("strokeWidthDp", shape.strokeWidthDp.toDouble())
                put("isFilled", shape.isFilled)
                put("cornerRadiusDp", shape.cornerRadiusDp.toDouble())
                put("rotation", shape.rotation.toDouble())
                put("scale", shape.scale.toDouble())
                put("positionX", shape.positionX.toDouble())
                put("positionY", shape.positionY.toDouble())
                put("timelineStartMs", shape.timelineStartMs)
                put("durationMs", shape.durationMs)
                put("animation", shape.animation.name)
            }
            shapesArray.put(obj)
        }
        root.put("shapes", shapesArray)

        val imagesArray = JSONArray()
        data.imageOverlays.forEach { img ->
            val obj = JSONObject().apply {
                put("id", img.id)
                put("name", img.name)
                put("uri", img.uri)
                put("timelineStartMs", img.timelineStartMs)
                put("durationMs", img.durationMs)
                put("positionX", img.positionX.toDouble())
                put("positionY", img.positionY.toDouble())
                put("scale", img.scale.toDouble())
                put("rotation", img.rotation.toDouble())
                put("alpha", img.alpha.toDouble())
                put("hasKeyframe", img.hasKeyframe)
                val kfArray = JSONArray()
                img.keyframes.forEach { kf ->
                    kfArray.put(JSONObject().apply {
                        put("id", kf.id)
                        put("timeOffsetMs", kf.timeOffsetMs)
                        put("scale", kf.scale.toDouble())
                        put("rotation", kf.rotation.toDouble())
                        put("alpha", kf.alpha.toDouble())
                        put("positionX", kf.positionX.toDouble())
                        put("positionY", kf.positionY.toDouble())
                        put("curve", kf.curve.name)
                    })
                }
                put("keyframes", kfArray)
            }
            imagesArray.put(obj)
        }
        root.put("imageOverlays", imagesArray)

        return root.toString()
    }

    fun deserialize(jsonString: String): TimelineData {
        if (jsonString.isBlank()) {
            return TimelineData(emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        }
        return try {
            val root = JSONObject(jsonString)

            val clips = mutableListOf<VideoClip>()
            val clipsArray = root.optJSONArray("clips")
            if (clipsArray != null) {
                for (i in 0 until clipsArray.length()) {
                    val obj = clipsArray.getJSONObject(i)
                    val stabObj = obj.optJSONObject("stabilization")
                    val stabSettings = if (stabObj != null) {
                        StabilizationSettings(
                            isEnabled = stabObj.optBoolean("isEnabled", false),
                            mode = try {
                                StabilizationMode.valueOf(stabObj.optString("mode", "SMOOTH_MOTION"))
                            } catch (e: Exception) {
                                StabilizationMode.SMOOTH_MOTION
                            },
                            smoothness = stabObj.optDouble("smoothness", 0.65).toFloat(),
                            cropMarginPercent = stabObj.optDouble("cropMarginPercent", 0.12).toFloat(),
                            reduceRollingShutter = stabObj.optBoolean("reduceRollingShutter", true)
                        )
                    } else {
                        StabilizationSettings()
                    }

                    val fxList = mutableListOf<VideoEffectItem>()
                    val fxArr = obj.optJSONArray("effects")
                    if (fxArr != null) {
                        for (f in 0 until fxArr.length()) {
                            val fObj = fxArr.getJSONObject(f)
                            fxList.add(
                                VideoEffectItem(
                                    id = fObj.optString("id", UUID.randomUUID().toString()),
                                    type = try {
                                        VideoEffectType.valueOf(fObj.optString("type", "VIGNETTE"))
                                    } catch (e: Exception) {
                                        VideoEffectType.VIGNETTE
                                    },
                                    intensity = fObj.optDouble("intensity", 1.0).toFloat(),
                                    secondaryParam = fObj.optDouble("secondaryParam", 0.5).toFloat(),
                                    isEnabled = fObj.optBoolean("isEnabled", true)
                                )
                            )
                        }
                    }

                    val kfList = mutableListOf<OpenShotKeyframe>()
                    val kfArr = obj.optJSONArray("keyframes")
                    if (kfArr != null) {
                        for (k in 0 until kfArr.length()) {
                            val kObj = kfArr.getJSONObject(k)
                            kfList.add(
                                OpenShotKeyframe(
                                    id = kObj.optString("id", UUID.randomUUID().toString()),
                                    timeOffsetMs = kObj.optLong("timeOffsetMs", 0L),
                                    scale = kObj.optDouble("scale", 1.0).toFloat(),
                                    rotation = kObj.optDouble("rotation", 0.0).toFloat(),
                                    alpha = kObj.optDouble("alpha", 1.0).toFloat(),
                                    positionX = kObj.optDouble("positionX", 0.0).toFloat(),
                                    positionY = kObj.optDouble("positionY", 0.0).toFloat(),
                                    curve = try {
                                        KeyframeCurve.valueOf(kObj.optString("curve", "BEZIER"))
                                    } catch (e: Exception) {
                                        KeyframeCurve.BEZIER
                                    }
                                )
                            )
                        }
                    }

                    val cgJson = obj.optJSONObject("colorGrading")
                    val colorGradingSettings = if (cgJson != null) {
                        com.example.data.model.ColorGradingSettings(
                            exposure = cgJson.optDouble("exposure", 0.0).toFloat(),
                            temperature = cgJson.optDouble("temperature", 0.0).toFloat(),
                            tint = cgJson.optDouble("tint", 0.0).toFloat(),
                            vibrance = cgJson.optDouble("vibrance", 0.0).toFloat(),
                            lut = try {
                                com.example.data.model.LutPreset.valueOf(cgJson.optString("lut", "NONE"))
                            } catch (e: Exception) {
                                com.example.data.model.LutPreset.NONE
                            },
                            liftR = cgJson.optDouble("liftR", 0.0).toFloat(),
                            liftG = cgJson.optDouble("liftG", 0.0).toFloat(),
                            liftB = cgJson.optDouble("liftB", 0.0).toFloat(),
                            gammaR = cgJson.optDouble("gammaR", 0.0).toFloat(),
                            gammaG = cgJson.optDouble("gammaG", 0.0).toFloat(),
                            gammaB = cgJson.optDouble("gammaB", 0.0).toFloat(),
                            gainR = cgJson.optDouble("gainR", 0.0).toFloat(),
                            gainG = cgJson.optDouble("gainG", 0.0).toFloat(),
                            gainB = cgJson.optDouble("gainB", 0.0).toFloat(),
                            liftPedestal = cgJson.optDouble("liftPedestal", 0.0).toFloat(),
                            gammaPedestal = cgJson.optDouble("gammaPedestal", 0.0).toFloat(),
                            gainPedestal = cgJson.optDouble("gainPedestal", 0.0).toFloat()
                        )
                    } else {
                        com.example.data.model.ColorGradingSettings()
                    }

                    clips.add(
                        VideoClip(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            uri = obj.optString("uri", ""),
                            sourceDurationMs = obj.getLong("sourceDurationMs"),
                            trimInMs = obj.optLong("trimInMs", 0L),
                            trimOutMs = obj.optLong("trimOutMs", obj.getLong("sourceDurationMs")),
                            timelineStartMs = obj.optLong("timelineStartMs", 0L),
                            speed = obj.optDouble("speed", 1.0).toFloat(),
                            volume = obj.optDouble("volume", 1.0).toFloat(),
                            isMuted = obj.optBoolean("isMuted", false),
                            filter = try {
                                VideoFilter.valueOf(obj.optString("filter", "NONE"))
                            } catch (e: Exception) {
                                VideoFilter.NONE
                            },
                            filterIntensity = obj.optDouble("filterIntensity", 1.0).toFloat(),
                            brightness = obj.optDouble("brightness", 0.0).toFloat(),
                            contrast = obj.optDouble("contrast", 1.0).toFloat(),
                            saturation = obj.optDouble("saturation", 1.0).toFloat(),
                            transitionIn = try {
                                TransitionType.valueOf(obj.optString("transitionIn", "NONE"))
                            } catch (e: Exception) {
                                TransitionType.NONE
                            },
                            transitionDurationMs = obj.optLong("transitionDurationMs", 600L),
                            scale = obj.optDouble("scale", 1.0).toFloat(),
                            rotation = obj.optDouble("rotation", 0.0).toFloat(),
                            flipHorizontal = obj.optBoolean("flipHorizontal", false),
                            isReversed = obj.optBoolean("isReversed", false),
                            isFreezeFrame = obj.optBoolean("isFreezeFrame", false),
                            hasKeyframe = obj.optBoolean("hasKeyframe", false),
                            keyframeScale = obj.optDouble("keyframeScale", 1.0).toFloat(),
                            canvasBgMode = try {
                                com.example.data.model.CanvasBgMode.valueOf(obj.optString("canvasBgMode", "BLUR"))
                            } catch (e: Exception) {
                                com.example.data.model.CanvasBgMode.BLUR
                            },
                            accentColorHex = obj.optLong("accentColorHex", 0xFF3B82F6),
                            isChromaKeyEnabled = obj.optBoolean("isChromaKeyEnabled", false),
                            chromaKeyColorHex = obj.optLong("chromaKeyColorHex", 0xFF00FF00),
                            chromaKeyTolerance = obj.optDouble("chromaKeyTolerance", 0.45).toFloat(),
                            stabilization = stabSettings,
                            effects = fxList,
                            keyframes = kfList,
                            alpha = obj.optDouble("alpha", 1.0).toFloat(),
                            positionX = obj.optDouble("positionX", 0.0).toFloat(),
                            positionY = obj.optDouble("positionY", 0.0).toFloat(),
                            isVertical = obj.optBoolean("isVertical", false),
                            canvasBlurRadiusDp = obj.optDouble("canvasBlurRadiusDp", 25.0).toFloat(),
                            canvasBgBrightness = obj.optDouble("canvasBgBrightness", 0.65).toFloat(),
                            hasSpeech = obj.optBoolean("hasSpeech", false),
                            colorGrading = colorGradingSettings,
                            cropLeft = obj.optDouble("cropLeft", 0.0).toFloat(),
                            cropTop = obj.optDouble("cropTop", 0.0).toFloat(),
                            cropRight = obj.optDouble("cropRight", 1.0).toFloat(),
                            cropBottom = obj.optDouble("cropBottom", 1.0).toFloat(),
                            resizeWidth = obj.optInt("resizeWidth", 0),
                            resizeHeight = obj.optInt("resizeHeight", 0),
                            fadeInMs = obj.optLong("fadeInMs", 0L),
                            fadeOutMs = obj.optLong("fadeOutMs", 0L),
                            width = obj.optInt("width", 0),
                            height = obj.optInt("height", 0),
                            frameRate = obj.optDouble("frameRate", 30.0).toFloat(),
                            videoCodec = obj.optString("videoCodec", ""),
                            mimeType = obj.optString("mimeType", ""),
                            hasAudio = obj.optBoolean("hasAudio", true),
                            fileSizeBytes = obj.optLong("fileSizeBytes", 0L),
                            thumbnailPath = obj.optString("thumbnailPath", "")
                        )
                    )
                }
            }

            val audioClips = mutableListOf<AudioTrackItem>()
            val audioArray = root.optJSONArray("audioClips")
            if (audioArray != null) {
                for (i in 0 until audioArray.length()) {
                    val obj = audioArray.getJSONObject(i)
                    val beatsList = mutableListOf<Long>()
                    val beatsArr = obj.optJSONArray("beatMarkersMs")
                    if (beatsArr != null) {
                        for (b in 0 until beatsArr.length()) {
                            beatsList.add(beatsArr.getLong(b))
                        }
                    }
                    audioClips.add(
                        AudioTrackItem(
                            id = obj.getString("id"),
                            name = obj.getString("name"),
                            uri = obj.optString("uri", ""),
                            timelineStartMs = obj.optLong("timelineStartMs", 0L),
                            durationMs = obj.optLong("durationMs", 5000L),
                            volume = obj.optDouble("volume", 1.0).toFloat(),
                            isMuted = obj.optBoolean("isMuted", false),
                            category = try {
                                AudioCategory.valueOf(obj.optString("category", "MUSIC"))
                            } catch (e: Exception) {
                                AudioCategory.MUSIC
                            },
                            fadeInMs = obj.optLong("fadeInMs", 0L),
                            fadeOutMs = obj.optLong("fadeOutMs", 0L),
                            isDuckingEnabled = obj.optBoolean("isDuckingEnabled", false),
                            accentColorHex = obj.optLong("accentColorHex", 0xFFF59E0B),
                            beatMarkersMs = beatsList,
                            isBeatSyncEnabled = obj.optBoolean("isBeatSyncEnabled", false),
                            pan = obj.optDouble("pan", 0.0).toFloat(),
                            fadeType = try {
                                AudioFadeType.valueOf(obj.optString("fadeType", "NONE"))
                            } catch (e: Exception) {
                                AudioFadeType.NONE
                            }
                        )
                    )
                }
            }

            val textOverlays = mutableListOf<TextOverlay>()
            val textArray = root.optJSONArray("textOverlays")
            if (textArray != null) {
                for (i in 0 until textArray.length()) {
                    val obj = textArray.getJSONObject(i)
                    textOverlays.add(
                        TextOverlay(
                            id = obj.getString("id"),
                            text = obj.getString("text"),
                            timelineStartMs = obj.optLong("timelineStartMs", 0L),
                            durationMs = obj.optLong("durationMs", 3000L),
                            positionX = obj.optDouble("positionX", 0.5).toFloat(),
                            positionY = obj.optDouble("positionY", 0.8).toFloat(),
                            fontSizeSp = obj.optInt("fontSizeSp", 24),
                            textColorHex = obj.optLong("textColorHex", 0xFFFFFFFF),
                            bgColorHex = obj.optLong("bgColorHex", 0xAA000000),
                            preset = try {
                                TextStylePreset.valueOf(obj.optString("preset", "CLASSIC"))
                            } catch (e: Exception) {
                                TextStylePreset.CLASSIC
                            },
                            subtitle = obj.optString("subtitle", ""),
                            borderWidthDp = obj.optDouble("borderWidthDp", 0.0).toFloat(),
                            borderColorHex = obj.optLong("borderColorHex", 0xFF000000),
                            is3D = obj.optBoolean("is3D", false),
                            template = obj.optString("template", "CLASSIC")
                        )
                    )
                }
            }

            val stickers = mutableListOf<OverlaySticker>()
            val stickersArray = root.optJSONArray("stickers")
            if (stickersArray != null) {
                for (i in 0 until stickersArray.length()) {
                    val obj = stickersArray.getJSONObject(i)
                    stickers.add(
                        OverlaySticker(
                            id = obj.getString("id"),
                            label = obj.getString("label"),
                            emojiOrIcon = obj.getString("emojiOrIcon"),
                            timelineStartMs = obj.optLong("timelineStartMs", 0L),
                            durationMs = obj.optLong("durationMs", 2500L),
                            positionX = obj.optDouble("positionX", 0.5).toFloat(),
                            positionY = obj.optDouble("positionY", 0.3).toFloat(),
                            scale = obj.optDouble("scale", 1.0).toFloat(),
                            isAnimated = obj.optBoolean("isAnimated", false),
                            animationType = obj.optString("animationType", "STATIC"),
                            loopAnimation = obj.optBoolean("loopAnimation", true)
                        )
                    )
                }
            }

            val shapes = mutableListOf<ShapeOverlay>()
            val shapesArray = root.optJSONArray("shapes")
            if (shapesArray != null) {
                for (i in 0 until shapesArray.length()) {
                    val obj = shapesArray.getJSONObject(i)
                    shapes.add(
                        ShapeOverlay(
                            id = obj.getString("id"),
                            shapeType = try {
                                ShapeType.valueOf(obj.optString("shapeType", "RECTANGLE"))
                            } catch (e: Exception) {
                                ShapeType.RECTANGLE
                            },
                            fillColorHex = obj.optLong("fillColorHex", 0xCC1E293B),
                            strokeColorHex = obj.optLong("strokeColorHex", 0xFF00E5FF),
                            strokeWidthDp = obj.optDouble("strokeWidthDp", 2.5).toFloat(),
                            isFilled = obj.optBoolean("isFilled", true),
                            cornerRadiusDp = obj.optDouble("cornerRadiusDp", 12.0).toFloat(),
                            rotation = obj.optDouble("rotation", 0.0).toFloat(),
                            scale = obj.optDouble("scale", 1.0).toFloat(),
                            positionX = obj.optDouble("positionX", 0.5).toFloat(),
                            positionY = obj.optDouble("positionY", 0.5).toFloat(),
                            timelineStartMs = obj.optLong("timelineStartMs", 0L),
                            durationMs = obj.optLong("durationMs", 3000L),
                            animation = try {
                                ShapeAnimation.valueOf(obj.optString("animation", "POP_IN"))
                            } catch (e: Exception) {
                                ShapeAnimation.POP_IN
                            }
                        )
                    )
                }
            }

            val imageOverlays = mutableListOf<ImageOverlay>()
            val imagesArray = root.optJSONArray("imageOverlays")
            if (imagesArray != null) {
                for (i in 0 until imagesArray.length()) {
                    val obj = imagesArray.getJSONObject(i)
                    val imgKeyframes = mutableListOf<OpenShotKeyframe>()
                    val kfArray = obj.optJSONArray("keyframes")
                    if (kfArray != null) {
                        for (j in 0 until kfArray.length()) {
                            val kObj = kfArray.getJSONObject(j)
                            imgKeyframes.add(
                                OpenShotKeyframe(
                                    id = kObj.optString("id", UUID.randomUUID().toString()),
                                    timeOffsetMs = kObj.optLong("timeOffsetMs", 0L),
                                    scale = kObj.optDouble("scale", 0.4).toFloat(),
                                    rotation = kObj.optDouble("rotation", 0.0).toFloat(),
                                    alpha = kObj.optDouble("alpha", 1.0).toFloat(),
                                    positionX = kObj.optDouble("positionX", 0.5).toFloat(),
                                    positionY = kObj.optDouble("positionY", 0.5).toFloat(),
                                    curve = try {
                                        KeyframeCurve.valueOf(kObj.optString("curve", "BEZIER"))
                                    } catch (_: Exception) {
                                        KeyframeCurve.BEZIER
                                    }
                                )
                            )
                        }
                    }
                    imageOverlays.add(
                        ImageOverlay(
                            id = obj.getString("id"),
                            name = obj.optString("name", "Image Overlay"),
                            uri = obj.optString("uri", ""),
                            timelineStartMs = obj.optLong("timelineStartMs", 0L),
                            durationMs = obj.optLong("durationMs", 4000L),
                            positionX = obj.optDouble("positionX", 0.5).toFloat(),
                            positionY = obj.optDouble("positionY", 0.5).toFloat(),
                            scale = obj.optDouble("scale", 0.4).toFloat(),
                            rotation = obj.optDouble("rotation", 0.0).toFloat(),
                            alpha = obj.optDouble("alpha", 1.0).toFloat(),
                            keyframes = imgKeyframes,
                            hasKeyframe = obj.optBoolean("hasKeyframe", imgKeyframes.isNotEmpty())
                        )
                    )
                }
            }

            TimelineData(clips, audioClips, textOverlays, stickers, shapes, imageOverlays)
        } catch (e: Exception) {
            TimelineData(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        }
    }
}
