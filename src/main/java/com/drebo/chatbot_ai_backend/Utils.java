package com.drebo.chatbot_ai_backend;

import com.drebo.chatbot_ai_backend.profiles.Mbt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Utils {

    public static int randomNumber(int min, int max){
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    public static Mbt randomMbt(){
        Random random = new Random();

        Mbt[] mbtValues = Mbt.values();

        int i = random.nextInt(mbtValues.length);
        return mbtValues[i];
    }

    public static String randomEthnicity(){
        Random random  = new Random();

        List<String> ethnicities = List.of(
                "Asian",
                "Hispanic",
                "Black",
                "White",
                "Indian",
                "South East Asian",
                "Middle Eastern",
                "Native American");

        int i = random.nextInt(ethnicities.size());
        return ethnicities.get(i);

//        Collections.shuffle(ethnicities);
//        return ethnicities.getFirst();
    }

    public static String randomSelfieTypes() {
        Random random = new Random();

        List<String> selfieTypes = List.of(
                "closeup with head and partial shoulders",
                "Reflection in a mirror",
                "action selfie, person in motion",
                "candid photo",
                "standing in nature",
                "sitting on a chair",
                "indoor photograph",
                "outdoor photograph"
        );

        int i = random.nextInt(selfieTypes.size());
        return selfieTypes.get(i);


//        Collections.shuffle(selfieTypes);
//        return selfieTypes.getFirst();
    }
}

