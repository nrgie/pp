package com.blueobject.peripatosapp.translation_engine;

import com.blueobject.peripatosapp.translation_engine.translators.IConvertor;
import com.blueobject.peripatosapp.translation_engine.translators.SpeechToTextConvertor;
import com.blueobject.peripatosapp.translation_engine.translators.TextToSpechConvertor;
import com.blueobject.peripatosapp.translation_engine.utils.ConversionCallback;


/**
 * Created by Hitesh on 12-07-2016.
 * <p/>
 * This Factory class return object of TTS or STT dependending on input enum TRANSLATOR_TYPE
 */
public class TranslatorFactory {

    private static TranslatorFactory ourInstance = new TranslatorFactory();

    public enum TRANSLATOR_TYPE {TEXT_TO_SPEECH, SPEECH_TO_TEXT}

    private TranslatorFactory() {
    }

    public static TranslatorFactory getInstance() {
        return ourInstance;
    }

    /**
     * Factory method to return object of STT or TTS
     *
     * @param translator_type
     * @param conversionCallback
     * @return
     */
    public IConvertor getTranslator(TRANSLATOR_TYPE translator_type, ConversionCallback conversionCallback) {
        switch (translator_type) {
            case TEXT_TO_SPEECH:

                //Get Text to speech translator
                return new TextToSpechConvertor(conversionCallback);

            case SPEECH_TO_TEXT:

                //Get speech to text translator
                return new SpeechToTextConvertor(conversionCallback);
        }

        return null;
    }
}
