package at.grid.sepp3.core.evaluation;

import at.grid.cms.attribute.KeyAttribute;
import at.grid.sepp3.core.app.SeppLogCollector;
import at.grid.sepp3.core.element.CmsHorizont;
import at.grid.sepp3.core.element.CmsProfil;
import java.util.ArrayList;

/**
 * This class calculates the complex parameters for a given CmsHorizont element.
 *
 * @author pk
 */
public class DS1HorizontComplexCalc {

  // ========================================================================
    // MEMBERS
    // ========================================================================
    /**
     * Profil for the horizont
     */
    private CmsProfil profil;
    /**
     * The horizon used for calculation
     */
    private CmsHorizont horizont;
    /**
     * Calculation log
     */
    SeppLogCollector log = new SeppLogCollector();

  // ========================================================================
    //  CONSTRUCTOR
    // ========================================================================
    public DS1HorizontComplexCalc(CmsProfil prf, CmsHorizont hrz) {
        this.profil = prf;
        this.horizont = hrz;
    }

//##############################################################################
//  HELPER METHODS
//##############################################################################
    public boolean isAttributeValueSet(String att) {
        String value = this.horizont.getAttributeValue(att);
        if (value != null && !"".equalsIgnoreCase(value)) {
            return true;
        } else {
            return false;
        }
    }

//##############################################################################
//  CALCULATION METHODS
//##############################################################################
    /**
     * Perform all calculation and write to output
     */
    public void calcAll() {

        log.add("Horizont '" + this.horizont.getAttributeValue("bezeichnung") + "' -> Berechnung der komplexen Parameter gestartet!");

        this.calculatePx01Hu();
        this.calculatePx02Sk();
        this.calculatePx03Ld();
        this.calculatePx03LdZ();
        this.calculatePx04FB();
        this.calculatePx05TM();
        this.calculatePx06HM();
        this.calculatePx07nFK();
        this.calculatePx08FK();
        this.calculatePx09LK();
        this.calculatePx10WSV();
        this.calculatePx11GPV();
        this.calculatePx12kf();
        this.calculatePx13KAKpot();
        this.calculatePx14KAKeff();
        this.calculatePx15Mb();

        log.add("Horizont '" + this.horizont.getAttributeValue("bezeichnung") + "' -> Berechnung der komplexen Parameter fertig!");
        log.add("============================================");
    }

    /**
     * Humusgehalt (Klassenmittelwert)
     */
    private void calculatePx01Hu() {
        // output
        log.add("1) " + this.horizont.getAttributeLabel("Px01_Hu"));
        // result value
        Float result = null;

        // get needed parameters
        long humusId = ((KeyAttribute) this.horizont.getAttribute("humus_klasse")).getId();
        Float humusWert = this.horizont.getAttributeValueFloat("humus_wert");

        if (this.horizont.hasAttributeValue("humus_wert")) {
//        if (humusWert >= 0) {
            log.add("   " + this.horizont.getAttributeLabel("humus_wert") + " gefunden");
            log.add("   -> " + humusWert + " %");
            log.add("   Laborwert wird übernommen.");
            result = humusWert;
        } else if (humusId >= 0) {
            log.add("   Klassenwert gefunden ('" + this.horizont.getAttributeLabel("humus_klasse") + "')");
            log.add("   -> " + ((KeyAttribute) this.horizont.getAttribute("humus_klasse")).getValue());
            log.add("   Wert (Zahl) wird für die Berechnung ermittelt");
            if (humusId == 0) {
                result = 0F;
            } else if (humusId == 10) {
                result = 0.5F;
            } else if (humusId == 20) {
                result = 1.5F;
            } else if (humusId == 30) {
                result = 3F;
            } else if (humusId == 40) {
                result = 6F;
            } else if (humusId == 50) {
                result = 12.5F;
            } else if (humusId == 60) {
                result = 22.5F;
            } else if (humusId == 70) {
                result = 50F;
            }
        } else {
            log.add("   FEHLER => Klassenwert und Labormessung fehlen!");
            return;
        }

        // result is not null, save value to db
        if (result != null) {
            // save to horizont
            this.horizont.setAttributeValue("Px01_Hu", String.valueOf(result));
            log.add("   ERGEBNIS => " + result + " %");
            return;
        }
    }

    /**
     * Skelettgehalt (Klassenmittelwert)
     */
    private void calculatePx02Sk() {
        // output
        log.add("-------------------");
        log.add("2) " + this.horizont.getAttributeLabel("Px02_Sk"));
        // result value
        Float result = null;

        // get needed parameters
        long skelettId = ((KeyAttribute) this.horizont.getAttribute("skelett_klasse")).getId();
        Float skelettWert = this.horizont.getAttributeValueFloat("skelett_wert");

        if (this.horizont.hasAttributeValue("skelett_wert")) {
//        if (skelettWert >= 0) {
            log.add("   " + this.horizont.getAttributeLabel("skelett_wert") + " gefunden");
            log.add("   -> " + skelettWert + " %");
            log.add("   Laborwert wird übernommen.");
            result = skelettWert;
        } else if (skelettId >= 0) {
            log.add("   " + this.horizont.getAttributeLabel("skelett_klasse") + " gefunden");
            log.add("   -> " + ((KeyAttribute) this.horizont.getAttribute("skelett_klasse")).getValue());
            log.add("   Wert (Zahl) wird für die Berechnung ermittelt");
            if (skelettId == 0) {
                result = 0F;
            } else if (skelettId == 199) {
                result = 5F;
            } else if (skelettId == 299) {
                result = 15F;
            } else if (skelettId == 399) {
                result = 30F;
            } else if (skelettId == 499) {
                result = 60F;
            } else if (skelettId == 599) {
                result = 90F;
            }
        } else {
            log.add("   FEHLER => Klassenwert und Labormessung fehlen!");
            return;
        }

        // result is not null, save value to db
        if (result != null) {
            // save to horizont
            this.horizont.setAttributeValue("Px02_Sk", String.valueOf(result));
            log.add("   ERGEBNIS => " + result + " %");
            return;
        }
    }

    /**
     * Lagerungsdichte (Klasse)
     */
    private void calculatePx03Ld() {
        // output
        log.add("-------------------");
        log.add("3) " + this.horizont.getAttributeLabel("Px03_Ld"));
        // result value
        String result = "";

        // get needed parameters
        long dichtecode = ((KeyAttribute) this.horizont.getAttribute("dichte_klasse")).getId();
        Float dichteWert = this.horizont.getAttributeValueFloat("dichte_wert");

        if (this.horizont.hasAttributeValue("dichte_wert")) {
//        if (dichteWert >= 0) {
            log.add("   " + this.horizont.getAttributeLabel("dichte_wert") + " gefunden");
            log.add("   -> " + dichteWert + " %");
            log.add("   Bewertungsklasse wird ermittelt [Ld1, Ld2, Ld3, Ld4, Ld4, Ld5]");
            if (dichteWert < 1.2) {
                result = "Ld1";
            } else if (dichteWert >= 1.2 && dichteWert < 1.4) {
                result = "Ld2";
            } else if (dichteWert >= 1.4 && dichteWert < 1.7) {
                result = "Ld3";
            } else if (dichteWert >= 1.7 && dichteWert <= 1.9) {
                result = "Ld4";
            } else if (dichteWert > 1.9) {
                result = "Ld5";
            }
        } else if (dichtecode >= 0) {
            log.add("   " + this.horizont.getAttributeLabel("dichte_klasse") + " gefunden");
            log.add("   -> " + ((KeyAttribute) this.horizont.getAttribute("dichte_klasse")).getValue());
            log.add("   Bewertungsklasse wird ermittelt [Ld1, Ld2, Ld3, Ld4, Ld4, Ld5]");
            if (dichtecode == 10) {
                result = "Ld1";
            } else if (dichtecode == 20) {
                result = "Ld2";
            } else if (dichtecode == 30 || dichtecode == 40) {
                result = "Ld3";
            } else if (dichtecode == 50) {
                result = "Ld4";
            } else if (dichtecode == 60) {
                result = "Ld5";
            }
        } else {
            log.add("   FEHLER => Klassenwert und Labormessung fehlen!");
            return;
        }

        // result is not null, save value to db
        if (!"".equalsIgnoreCase(result)) {
            // save to horizont
            this.horizont.setAttributeValue("Px03_Ld", String.valueOf(result));
            log.add("   ERGEBNIS => " + result);
        }
    }

    /**
     * Lagerungsdichte (Wert)
     */
    private void calculatePx03LdZ() {
        // output
        log.add("-------------------");
        log.add("4) " + this.horizont.getAttributeLabel("Px03a_LdZ"));
        // result value
        Float result = null;

        // get needed parameters
        Float dichteWert = this.horizont.getAttributeValueFloat("dichte_wert");
        String px03 = this.horizont.getAttributeValue("Px03_Ld");

        if (this.horizont.hasAttributeValue("dichte_wert")) {
//        if (dichteWert >= 0) {
            log.add("   " + this.horizont.getAttributeLabel("dichte_wert") + " gefunden");
            log.add("   -> " + dichteWert + " g/cm³");
            log.add("   Laborwert wird übernommen.");
            result = dichteWert;
        } else if (px03 != null && !"".equalsIgnoreCase(px03)) {
            log.add("   Komplexer Parameter '" + this.horizont.getAttributeLabel("Px03_Ld") + "' gefunden");
            log.add("   -> " + px03);
            log.add("   Wert (Zahl) wird für die Berechnung ermittelt");
            if (px03.equalsIgnoreCase("Ld1")) {
                result = 1.0f;
            } else if (px03.equalsIgnoreCase("Ld2")) {
                result = 1.3f;
            } else if (px03.equalsIgnoreCase("Ld3")) {
                result = 1.55f;
            } else if (px03.equalsIgnoreCase("Ld4")) {
                result = 1.8f;
            } else if (px03.equalsIgnoreCase("Ld5")) {
                result = 2.0f;
            }
        } else {
            log.add("   FEHLER => Laborwert und komplexer Parameter fehlen!");
            return;
        }

        // result is not null, save value to db
        if (result != null) {
            // save to horizont
            this.horizont.setAttributeValue("Px03a_LdZ", String.valueOf(result));
            log.add("   ERGEBNIS => " + result + " g/cm³");
        }
    }

    /**
     * Feinbodenmenge
     */
    private void calculatePx04FB() {
        // output
        log.add("-------------------");
        log.add("5) " + this.horizont.getAttributeLabel("Px04_FB"));
        // check px02
        if (!isAttributeValueSet("Px02_Sk")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px02_Sk"));
            return;
        }

        // result value
        Integer result = null;

        // get needed parameters
        Float skelett = this.horizont.getAttributeValueFloat("Px02_Sk");

        int maechtigkeit = this.horizont.getThickness();
        Float dichteWert = this.horizont.getAttributeValueFloat("dichte_wert");
        String px03 = this.horizont.getAttributeValue("Px03_Ld");

        if (this.horizont.hasAttributeValue("dichte_wert")) {
//        if (dichteWert >= 0) {
            log.add("   " + this.horizont.getAttributeLabel("dichte_wert") + " gefunden");
            log.add("   -> " + dichteWert + " g/cm³");
            log.add("   Mächtigkeit des Horizontes berechnet");
            log.add("   -> " + maechtigkeit + " cm");
            log.add("   Berechnung anhand des Laborwertes der Lagerungsdichte, der Horizontmächtigkeit und des Skelettgehaltes");

            result = Math.round(dichteWert * maechtigkeit * 10 * (100 - skelett) / 100);

        } else if (px03 != null && !"".equalsIgnoreCase(px03)) {
            log.add("   Komplexer Parameter '" + this.horizont.getAttributeLabel("Px03_Ld") + "' gefunden");
            log.add("   -> " + px03);
            log.add("   Mächtigkeit des Horizontes berechnet");
            log.add("   -> " + maechtigkeit + " cm");
            log.add("   Berechnung anhand der Lagerungsdichteklasse, der Horizontmächtigkeit und des Skelettgehaltes");

            if (px03.equalsIgnoreCase("Ld1")) {
                result = Math.round(1.0f * maechtigkeit * 10 * (100 - skelett) / 100);
            } else if (px03.equalsIgnoreCase("Ld2")) {
                result = Math.round(1.3f * maechtigkeit * 10 * (100 - skelett) / 100);
            } else if (px03.equalsIgnoreCase("Ld3")) {
                result = Math.round(1.55f * maechtigkeit * 10 * (100 - skelett) / 100);
            } else if (px03.equalsIgnoreCase("Ld4")) {
                result = Math.round(1.8f * maechtigkeit * 10 * (100 - skelett) / 100);
            } else if (px03.equalsIgnoreCase("Ld5")) {
                result = Math.round(2.0f * maechtigkeit * 10 * (100 - skelett) / 100);
            }
        } else {
            log.add("   FEHLER => Laborwert der Lagerungsdichte und komplexer Parameter '" + this.horizont.getAttributeLabel("dichte_wert") + "' fehlen!");
            return;
        }

        // result is not null, save value to db
        if (result != null) {
            // save to horizont
            this.horizont.setAttributeValue("Px04_FB", String.valueOf(result));
            log.add("   ERGEBNIS => " + result + " kg/m³");
        }
    }

    /**
     * Tonmenge
     */
    private void calculatePx05TM() {
        // output
        log.add("-------------------");
        log.add("6) " + this.horizont.getAttributeLabel("Px05_TM"));
        // check Px04_FB
        if (!isAttributeValueSet("Px04_FB")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px04_FB"));
            return;
        }

        // result value
        Integer result = null;

        // get needed parameters
        float fb = this.horizont.getAttributeValueFloat("Px04_FB");

        long bodenartCode = ((KeyAttribute) this.horizont.getAttribute("bodenart")).getId();
        long bodenartlaborCode = ((KeyAttribute) this.horizont.getAttribute("bodenartlabor")).getId();
        long bodenart = -1;

        float tongehalt = this.horizont.getAttributeValueFloat("ton");

//    if (tongehalt >= 0) {
        if (this.horizont.hasAttributeValue("ton")) {
            log.add("   " + this.horizont.getAttributeLabel("ton") + "  gefunden");
            log.add("   -> " + tongehalt);
            log.add("   Berechnung anhand der Feinbodenmenge und des Tongehalts.");
            result = Math.round(fb * tongehalt);
        } else {
            log.add("   Tongehalt nicht gefunden, es wird versucht, diesen über die Bodenart zu bestimmen...");
            // define bodenart
            if (bodenartlaborCode >= 0) {
                log.add("   " + this.horizont.getAttributeLabel("bodenartlabor") + "  gefunden");
                log.add("   -> " + ((KeyAttribute) this.horizont.getAttribute("bodenartlabor")).getValue());
                log.add("   Für die Bestimmung des Tongehaltes wird diese Klasse verwendet");
                bodenart = bodenartlaborCode;
            } else if (bodenartCode >= 0) {
                log.add("   " + this.horizont.getAttributeLabel("bodenart") + "  gefunden");
                log.add("   -> " + ((KeyAttribute) this.horizont.getAttribute("bodenart")).getValue());
                log.add("   Für die Bestimmung des Tongehaltes wird diese Klasse verwendet");
                bodenart = bodenartCode;
            } else {
                log.add("   FEHLER => Tongehalt und Angabe zur Bodenart fehlen!");
                return;
            }

            if (bodenart == 101 || bodenart == 121) {
                tongehalt = 2.5f;
            } else if (bodenart == 212) {
                tongehalt = 7.5f;
            } else if (bodenart == 202 || bodenart == 231) {
                tongehalt = 10f;
            } else if (bodenart == 332 || bodenart == 341 || bodenart == 313) {
                tongehalt = 20f;
            } else if (bodenart == 423 || bodenart == 403 || bodenart == 414) {
                tongehalt = 30f;
            } else if (bodenart == 534) {
                tongehalt = 45f;
            } else if (bodenart == 504) {
                tongehalt = 70f;
            }
            result = Math.round(fb * (tongehalt / 100));
        }

        // result is not null, save value to db
        if (result != null) {
            // save to horizont
            this.horizont.setAttributeValue("Px05_TM", String.valueOf(result));
            log.add("   ERGEBNIS => " + result + " kg/m³");
        }
    }

    /**
     * Humusmenge
     */
    private void calculatePx06HM() {
        // output
        log.add("-------------------");
        log.add("7) " + this.horizont.getAttributeLabel("Px06_HM"));
        // check Px01_Hu and px04
        if (!isAttributeValueSet("Px01_Hu")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px01_Hu"));
            return;
        }
        if (!isAttributeValueSet("Px04_FB")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px04_FB"));
            return;
        }

        // result value
        Float result = null;

        // get needed parameters
        float humusgehalt = this.horizont.getAttributeValueFloat("Px01_Hu");
        float feinboden = this.horizont.getAttributeValueFloat("Px04_FB");

        log.add("   " + this.horizont.getAttributeLabel("Px01_Hu") + "  gefunden");
        log.add("   -> " + humusgehalt + " %");
        log.add("   " + this.horizont.getAttributeLabel("Px04_FB") + "  gefunden");
        log.add("   -> " + feinboden + " kg/m³");
        log.add("   Berechnung anhand des Humusgehaltes und der Feinbodenmenge");
        result = (float)Math.round(10 * feinboden * humusgehalt / 100) / 10;

        // result is not null, save value to db
        if (result != null) {
            // save to horizont
            this.horizont.setAttributeValue("Px06_HM", String.valueOf(result));
            log.add("   ERGEBNIS => " + result + " kg/m³");
        }
    }

    /**
     * Nutzbare Feldkapazität [l/m²]
     */
    private void calculatePx07nFK() {
        // output
        log.add("-------------------");
        log.add("8) " + this.horizont.getAttributeLabel("Px07_nFK"));
        // check px01, px02 and px03
        if (!isAttributeValueSet("Px01_Hu")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px01_Hu"));
            return;
        }
        if (!isAttributeValueSet("Px02_Sk")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px02_Sk"));
            return;
        }
        if (!isAttributeValueSet("Px03_Ld")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px03_Ld"));
            return;
        }

        // result value
        Integer result = null;

        // get needed parameters
        float humus = this.horizont.getAttributeValueFloat("Px01_Hu");
        float skelett = this.horizont.getAttributeValueFloat("Px02_Sk");
        String dichte = this.horizont.getAttributeValue("Px03_Ld");
        int maechtigkeit = this.horizont.getThickness();

        long bodenartCode = ((KeyAttribute) this.horizont.getAttribute("bodenart")).getId();
        long bodenartLaborCode = ((KeyAttribute) this.horizont.getAttribute("bodenartlabor")).getId();
        long bodenart = -1;

        // define bodenart
        if (bodenartLaborCode >= 0) {
            log.add("   " + this.horizont.getAttributeLabel("bodenartlabor") + "  gefunden");
            log.add("   -> " + ((KeyAttribute) this.horizont.getAttribute("bodenartlabor")).getValue());
            bodenart = bodenartLaborCode;
        } else if (bodenartCode >= 0) {
            log.add("   " + this.horizont.getAttributeLabel("bodenart") + "  gefunden");
            log.add("   -> " + ((KeyAttribute) this.horizont.getAttribute("bodenart")).getValue());
            bodenart = bodenartCode;
        } else {
            log.add("   FEHLER => Angabe zur Bodenart fehlt!");
            return;
        }

        float nfk = -1;

        log.add("   " + this.horizont.getAttributeLabel("Px03_Ld") + " -> " + dichte);
        int category = 0;
        if (dichte.equalsIgnoreCase("Ld1") || dichte.equalsIgnoreCase("Ld2")) {
            category = 1;
        } else if (dichte.equalsIgnoreCase("Ld3")) {
            category = 2;
        } else if (dichte.equalsIgnoreCase("Ld4") || dichte.equalsIgnoreCase("Ld5")) {
            category = 3;
        }

        if (bodenart == 101 && category == 1) {
            nfk = 16;
        } else if (bodenart == 101 && category == 2) {
            nfk = 14.5f;
        } else if (bodenart == 101 && category == 3) {
            nfk = 12;
        } else if (bodenart == 121 && category == 1) {
            nfk = 26;
        } else if (bodenart == 121 && category == 2) {
            nfk = 23;
        } else if (bodenart == 121 && category == 3) {
            nfk = 20;
        } else if (bodenart == 212 && category == 1) {
            nfk = 26;
        } else if (bodenart == 212 && category == 2) {
            nfk = 23.5f;
        } else if (bodenart == 212 && category == 3) {
            nfk = 21;
        } else if (bodenart == 202 && category == 1) {
            nfk = 26;
        } else if (bodenart == 202 && category == 2) {
            nfk = 14;
        } else if (bodenart == 202 && category == 3) {
            nfk = 22.5f;
        } else if (bodenart == 231 && category == 1) {
            nfk = 25;
        } else if (bodenart == 231 && category == 2) {
            nfk = 21;
        } else if (bodenart == 231 && category == 3) {
            nfk = 18;
        } else if (bodenart == 332 && category == 1) {
            nfk = 22;
        } else if (bodenart == 332 && category == 2) {
            nfk = 19;
        } else if (bodenart == 332 && category == 3) {
            nfk = 17;
        } else if (bodenart == 341 && category == 1) {
            nfk = 19;
        } else if (bodenart == 341 && category == 2) {
            nfk = 15;
        } else if (bodenart == 341 && category == 3) {
            nfk = 13;
        } else if (bodenart == 313 && category == 1) {
            nfk = 22;
        } else if (bodenart == 313 && category == 2) {
            nfk = 17;
        } else if (bodenart == 313 && category == 3) {
            nfk = 14.5f;
        } else if (bodenart == 423 && category == 1) {
            nfk = 20.5f;
        } else if (bodenart == 423 && category == 2) {
            nfk = 16;
        } else if (bodenart == 423 && category == 3) {
            nfk = 12.5f;
        } else if (bodenart == 403 && category == 1) {
            nfk = 19;
        } else if (bodenart == 403 && category == 2) {
            nfk = 15.5f;
        } else if (bodenart == 403 && category == 3) {
            nfk = 11.5f;
        } else if (bodenart == 414 && category == 1) {
            nfk = 18;
        } else if (bodenart == 414 && category == 2) {
            nfk = 15.5f;
        } else if (bodenart == 414 && category == 3) {
            nfk = 11.5f;
        } else if (bodenart == 534 && category == 1) {
            nfk = 21;
        } else if (bodenart == 534 && category == 2) {
            nfk = 14.5f;
        } else if (bodenart == 534 && category == 3) {
            nfk = 11;
        } else if (bodenart == 504 && category == 1) {
            nfk = 21.5f;
        } else if (bodenart == 504 && category == 2) {
            nfk = 14.5f;
        } else if (bodenart == 504 && category == 3) {
            nfk = 11;
        }
        log.add("   Nutzbare Feldkapazität anhand der Lagerungsdichte und der Bodenart ermittelt");
        log.add("   nFK -> " + nfk + " l/m²");

        // correct value with Humusgehalt
        log.add("   Nutzbare Feldkapazität wird anhand des Humusgehaltes und der Bodenart korrigiert");
        log.add("   " + this.horizont.getAttributeLabel("Px01_Hu") + " -> " + humus + " %");

        if ((bodenart == 101 || bodenart == 121) && humus < 1) {
            nfk = nfk + 0;
        } else if ((bodenart == 101 || bodenart == 121) && (humus >= 1 && humus < 2)) {
            nfk = nfk + 0.5f;
        } else if ((bodenart == 101 || bodenart == 121) && (humus >= 2 && humus < 4)) {
            nfk = nfk + 1;
        } else if ((bodenart == 101 || bodenart == 121) && (humus >= 4 && humus < 8)) {
            nfk = nfk + 3;
        } else if ((bodenart == 101 || bodenart == 121) && (humus >= 8 && humus < 15)) {
            nfk = nfk + 3.5f;
        } else if ((bodenart == 231 || bodenart == 341) && humus < 1) {
            nfk = nfk + 0;
        } else if ((bodenart == 231 || bodenart == 341) && (humus >= 1 && humus < 2)) {
            nfk = nfk + 0.5f;
        } else if ((bodenart == 231 || bodenart == 341) && (humus >= 2 && humus < 4)) {
            nfk = nfk + 1;
        } else if ((bodenart == 231 || bodenart == 341) && (humus >= 4 && humus < 8)) {
            nfk = nfk + 3;
        } else if ((bodenart == 231 || bodenart == 341) && (humus >= 8 && humus < 15)) {
            nfk = nfk + 4;
        } else if ((bodenart == 202 || bodenart == 212) && humus < 1) {
            nfk = nfk + 0;
        } else if ((bodenart == 202 || bodenart == 212) && (humus >= 1 && humus < 2)) {
            nfk = nfk + 0.5f;
        } else if ((bodenart == 202 || bodenart == 212) && (humus >= 2 && humus < 4)) {
            nfk = nfk + 1;
        } else if ((bodenart == 202 || bodenart == 212) && (humus >= 4 && humus < 8)) {
            nfk = nfk + 3.5f;
        } else if ((bodenart == 202 || bodenart == 212) && (humus >= 8 && humus < 15)) {
            nfk = nfk + 4.5f;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && humus < 1) {
            nfk = nfk + 0;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && (humus >= 1 && humus < 2)) {
            nfk = nfk + 0.5f;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && (humus >= 2 && humus < 4)) {
            nfk = nfk + 1.5f;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && (humus >= 4 && humus < 8)) {
            nfk = nfk + 4;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && (humus >= 8 && humus < 15)) {
            nfk = nfk + 7;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && humus < 1) {
            nfk = nfk + 0;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && (humus >= 1 && humus < 2)) {
            nfk = nfk + 1;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && (humus >= 2 && humus < 4)) {
            nfk = nfk + 2.5f;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && (humus >= 4 && humus < 8)) {
            nfk = nfk + 5.5f;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && (humus >= 8 && humus < 15)) {
            nfk = nfk + 10;
        }

        if (humus > 30) {
            nfk = 50;
        } else if (humus > 15 && humus <= 30) {
            nfk = 37;
        }

        log.add("   Korrigierte nFK -> " + nfk + " l/m²");

        if (nfk >= 0) {
            log.add("   Berechnung anhand der Horizontmächtigkeit, des Skelettgehaltes und der nFK");
            result = Math.round(maechtigkeit * 10 * (100 - skelett) / 100 * (nfk / 100));
        } else {
            log.add("   FEHLER => Konnte nicht ermittelt werden! [nFK<0]");
            return;
        }

        // result is not null, save value to db
        if (result != null) {
            // save to horizont
            this.horizont.setAttributeValue("Px07_nFK", String.valueOf(result));
            log.add("   ERGEBNIS => " + result + " l/m²");
        }
    }

    /**
     * Feldkapazität
     */
    private void calculatePx08FK() {
        // output
        log.add("-------------------");
        log.add("9) " + this.horizont.getAttributeLabel("Px08_FK"));
        // check px01, px02 and px03
        if (!isAttributeValueSet("Px01_Hu")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px01_Hu"));
            return;
        }
        if (!isAttributeValueSet("Px02_Sk")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px02_Sk"));
            return;
        }
        if (!isAttributeValueSet("Px03_Ld")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px03_Ld"));
            return;
        }

        // result value
        Integer result = null;

        // get needed parameters
        float humus = this.horizont.getAttributeValueFloat("Px01_Hu");
        float skelett = this.horizont.getAttributeValueFloat("Px02_Sk");
        String dichte = this.horizont.getAttributeValue("Px03_Ld");
        int maechtigkeit = this.horizont.getThickness();

        long bodenartCode = ((KeyAttribute) this.horizont.getAttribute("bodenart")).getId();
        long bodenartLaborCode = ((KeyAttribute) this.horizont.getAttribute("bodenartlabor")).getId();
        long bodenart = -1;

        // define bodenart
        if (bodenartLaborCode >= 0) {
            log.add("   " + this.horizont.getAttributeLabel("bodenartlabor") + "  gefunden");
            log.add("   -> " + ((KeyAttribute) this.horizont.getAttribute("bodenartlabor")).getValue());
            bodenart = bodenartLaborCode;
        } else if (bodenartCode >= 0) {
            log.add("   " + this.horizont.getAttributeLabel("bodenart") + "  gefunden");
            log.add("   -> " + ((KeyAttribute) this.horizont.getAttribute("bodenart")).getValue());
            bodenart = bodenartCode;
        } else {
            log.add("   FEHLER => Angabe zur Bodenart fehlt!");
            return;
        }

        float fk = -1;

        log.add("   " + this.horizont.getAttributeLabel("Px03_Ld") + " -> " + dichte);
        int category = 0;
        if (dichte.equalsIgnoreCase("Ld1") || dichte.equalsIgnoreCase("Ld2")) {
            category = 1;
        } else if (dichte.equalsIgnoreCase("Ld3")) {
            category = 2;
        } else if (dichte.equalsIgnoreCase("Ld4") || dichte.equalsIgnoreCase("Ld5")) {
            category = 3;
        }

        String bodenartStr = this.horizont.getApplication().getKeytable("bodenart").getTerm(bodenart, horizont.getUser().getCurrentLanguage());
        log.add("   Bodenart -> " + bodenartStr);
        if (bodenart == 101 && category == 1) {
            fk = 23;
        } else if (bodenart == 101 && category == 2) {
            fk = 20;
        } else if (bodenart == 101 && category == 3) {
            fk = 17.5f;
        } else if (bodenart == 121 && category == 1) {
            fk = 36;
        } else if (bodenart == 121 && category == 2) {
            fk = 30;
        } else if (bodenart == 121 && category == 3) {
            fk = 28;
        } else if (bodenart == 212 && category == 1) {
            fk = 39;
        } else if (bodenart == 212 && category == 2) {
            fk = 33.5f;
        } else if (bodenart == 212 && category == 3) {
            fk = 31;
        } else if (bodenart == 202 && category == 1) {
            fk = 39;
        } else if (bodenart == 202 && category == 2) {
            fk = 35.5f;
        } else if (bodenart == 202 && category == 3) {
            fk = 33.5f;
        } else if (bodenart == 231 && category == 1) {
            fk = 35.5f;
        } else if (bodenart == 231 && category == 2) {
            fk = 30.5f;
        } else if (bodenart == 231 && category == 3) {
            fk = 27.5f;
        } else if (bodenart == 332 && category == 1) {
            fk = 41.5f;
        } else if (bodenart == 332 && category == 2) {
            fk = 36;
        } else if (bodenart == 332 && category == 3) {
            fk = 33;
        } else if (bodenart == 341 && category == 1) {
            fk = 29;
        } else if (bodenart == 341 && category == 2) {
            fk = 26.5f;
        } else if (bodenart == 341 && category == 3) {
            fk = 26;
        } else if (bodenart == 313 && category == 1) {
            fk = 41;
        } else if (bodenart == 313 && category == 2) {
            fk = 34;
        } else if (bodenart == 313 && category == 3) {
            fk = 30.5f;
        } else if (bodenart == 423 && category == 1) {
            fk = 47;
        } else if (bodenart == 423 && category == 2) {
            fk = 38.5f;
        } else if (bodenart == 423 && category == 3) {
            fk = 34;
        } else if (bodenart == 403 && category == 1) {
            fk = 48.5f;
        } else if (bodenart == 403 && category == 2) {
            fk = 39.5f;
        } else if (bodenart == 403 && category == 3) {
            fk = 35;
        } else if (bodenart == 414 && category == 1) {
            fk = 42;
        } else if (bodenart == 414 && category == 2) {
            fk = 36;
        } else if (bodenart == 414 && category == 3) {
            fk = 31;
        } else if (bodenart == 534 && category == 1) {
            fk = 56.5f;
        } else if (bodenart == 534 && category == 2) {
            fk = 48;
        } else if (bodenart == 534 && category == 3) {
            fk = 42;
        } else if (bodenart == 504 && category == 1) {
            fk = 58;
        } else if (bodenart == 504 && category == 2) {
            fk = 49;
        } else if (bodenart == 504 && category == 3) {
            fk = 42.5f;
        }
        log.add("   Feldkapazität anhand der Lagerungsdichte und der Bodenart ermittelt");
        log.add("   FK -> " + fk + " l/m²");

        // correct value with Humusgehalt
        log.add("   Feldkapazität wird anhand des Humusgehaltes und der Bodenart korrigiert");
        log.add("   " + this.horizont.getAttributeLabel("Px01_Hu") + " -> " + humus + " %");

        if ((bodenart == 101 || bodenart == 121) && humus < 1) {
            fk = fk + 0;
        } else if ((bodenart == 101 || bodenart == 121) && (humus >= 1 && humus < 2)) {
            fk = fk + 1.5f;
        } else if ((bodenart == 101 || bodenart == 121) && (humus >= 2 && humus < 4)) {
            fk = fk + 3.5f;
        } else if ((bodenart == 101 || bodenart == 121) && (humus >= 4 && humus < 8)) {
            fk = fk + 7.5f;
        } else if ((bodenart == 101 || bodenart == 121) && (humus >= 8 && humus < 15)) {
            fk = fk + 10;
        } else if ((bodenart == 231 || bodenart == 341) && humus < 1) {
            fk = fk + 0;
        } else if ((bodenart == 231 || bodenart == 341) && (humus >= 1 && humus < 2)) {
            fk = fk + 1.5f;
        } else if ((bodenart == 231 || bodenart == 341) && (humus >= 2 && humus < 4)) {
            fk = fk + 3.5f;
        } else if ((bodenart == 231 || bodenart == 341) && (humus >= 4 && humus < 8)) {
            fk = fk + 8;
        } else if ((bodenart == 231 || bodenart == 341) && (humus >= 8 && humus < 15)) {
            fk = fk + 11.5f;
        } else if ((bodenart == 202 || bodenart == 212) && humus < 1) {
            fk = fk + 0;
        } else if ((bodenart == 202 || bodenart == 212) && (humus >= 1 && humus < 2)) {
            fk = fk + 1.5f;
        } else if ((bodenart == 202 || bodenart == 212) && (humus >= 2 && humus < 4)) {
            fk = fk + 3.5f;
        } else if ((bodenart == 202 || bodenart == 212) && (humus >= 4 && humus < 8)) {
            fk = fk + 7.5f;
        } else if ((bodenart == 202 || bodenart == 212) && (humus >= 8 && humus < 15)) {
            fk = fk + 10;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && humus < 1) {
            fk = fk + 0;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && (humus >= 1 && humus < 2)) {
            fk = fk + 2.5f;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && (humus >= 2 && humus < 4)) {
            fk = fk + 4;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && (humus >= 4 && humus < 8)) {
            fk = fk + 10;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && (humus >= 8 && humus < 15)) {
            fk = fk + 13.5f;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && humus < 1) {
            fk = fk + 0;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && (humus >= 1 && humus < 2)) {
            fk = fk + 3;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && (humus >= 2 && humus < 4)) {
            fk = fk + 6;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && (humus >= 4 && humus < 8)) {
            fk = fk + 11.5f;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && (humus >= 8 && humus < 15)) {
            fk = fk + 17;
        }

        if (humus > 30) {
            fk = 67;
        } else if (humus > 15 && humus <= 30) {
            fk = 56;
        }

        log.add("   Korrigierte FK -> " + fk + " l/m²");

        if (fk >= 0) {
            log.add("   Berechnung anhand der Horizontmächtigkeit, des Skelettgehaltes und der FK");
            result = Math.round(maechtigkeit * 10 * (100 - skelett) / 100 * (fk / 100));
        } else {
            log.add("   FEHLER => Konnte nicht ermittelt werden! [FK<0]");
            return;
        }

        // result is not null, save value to db
        if (result != null) {
            // save to horizont
            this.horizont.setAttributeValue("Px08_FK", String.valueOf(result));
            log.add("   ERGEBNIS => " + result + " l/m²");
        }
    }

    /**
     * Luftkapazität [l/m²]
     */
    private void calculatePx09LK() {
        // output
        log.add("-------------------");
        log.add("10) " + this.horizont.getAttributeLabel("Px09_LK"));
        // check px01, px02 and px03
        if (!isAttributeValueSet("Px01_Hu")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px01_Hu"));
            return;
        }
        if (!isAttributeValueSet("Px02_Sk")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px02_Sk"));
            return;
        }
        if (!isAttributeValueSet("Px03_Ld")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px03_Ld"));
            return;
        }

        // result value
        Integer result = null;

        // get needed parameters
        float humus = this.horizont.getAttributeValueFloat("Px01_Hu");
        float skelett = this.horizont.getAttributeValueFloat("Px02_Sk");
        String dichte = this.horizont.getAttributeValue("Px03_Ld");
        int maechtigkeit = this.horizont.getThickness();

        long bodenartCode = ((KeyAttribute) this.horizont.getAttribute("bodenart")).getId();
        long bodenartLaborCode = ((KeyAttribute) this.horizont.getAttribute("bodenartlabor")).getId();
        long bodenart = -1;

        // define bodenart
        if (bodenartLaborCode >= 0) {
            log.add("   " + this.horizont.getAttributeLabel("bodenartlabor") + "  gefunden");
            log.add("   -> " + ((KeyAttribute) this.horizont.getAttribute("bodenartlabor")).getValue());
            bodenart = bodenartLaborCode;
        } else if (bodenartCode >= 0) {
            log.add("   " + this.horizont.getAttributeLabel("bodenart") + "  gefunden");
            log.add("   -> " + ((KeyAttribute) this.horizont.getAttribute("bodenart")).getValue());
            bodenart = bodenartCode;
        } else {
            log.add("   FEHLER => Angabe zur Bodenart fehlt!");
            return;
        }

        float lk = -1;

        log.add("   " + this.horizont.getAttributeLabel("Px03_Ld") + " -> " + dichte);
        int category = 0;
        if (dichte.equalsIgnoreCase("Ld1") || dichte.equalsIgnoreCase("Ld2")) {
            category = 1;
        } else if (dichte.equalsIgnoreCase("Ld3")) {
            category = 2;
        } else if (dichte.equalsIgnoreCase("Ld4") || dichte.equalsIgnoreCase("Ld5")) {
            category = 3;
        }

        String bodenartStr = this.horizont.getApplication().getKeytable("bodenart").getTerm(bodenart, horizont.getUser().getCurrentLanguage());
        log.add("   Bodenart -> " + bodenartStr);
        if (bodenart == 101 && category == 1) {
            lk = 19;
        } else if (bodenart == 101 && category == 2) {
            lk = 16;
        } else if (bodenart == 101 && category == 3) {
            lk = 13;
        } else if (bodenart == 121 && category == 1) {
            lk = 9.5f;
        } else if (bodenart == 121 && category == 2) {
            lk = 7;
        } else if (bodenart == 121 && category == 3) {
            lk = 4;
        } else if (bodenart == 212 && category == 1) {
            lk = 8.5f;
        } else if (bodenart == 212 && category == 2) {
            lk = 5.5f;
        } else if (bodenart == 212 && category == 3) {
            lk = 3;
        } else if (bodenart == 202 && category == 1) {
            lk = 8.5f;
        } else if (bodenart == 202 && category == 2) {
            lk = 4.5f;
        } else if (bodenart == 202 && category == 3) {
            lk = 2;
        } else if (bodenart == 231 && category == 1) {
            lk = 10;
        } else if (bodenart == 231 && category == 2) {
            lk = 7.5f;
        } else if (bodenart == 231 && category == 3) {
            lk = 5;
        } else if (bodenart == 332 && category == 1) {
            lk = 8;
        } else if (bodenart == 332 && category == 2) {
            lk = 6;
        } else if (bodenart == 332 && category == 3) {
            lk = 3.5f;
        } else if (bodenart == 341 && category == 1) {
            lk = 15;
        } else if (bodenart == 341 && category == 2) {
            lk = 12;
        } else if (bodenart == 341 && category == 3) {
            lk = 9;
        } else if (bodenart == 313 && category == 1) {
            lk = 8.5f;
        } else if (bodenart == 313 && category == 2) {
            lk = 6.5f;
        } else if (bodenart == 313 && category == 3) {
            lk = 5;
        } else if (bodenart == 423 && category == 1) {
            lk = 7;
        } else if (bodenart == 423 && category == 2) {
            lk = 5.5f;
        } else if (bodenart == 423 && category == 3) {
            lk = 4;
        } else if (bodenart == 403 && category == 1) {
            lk = 7;
        } else if (bodenart == 403 && category == 2) {
            lk = 5;
        } else if (bodenart == 403 && category == 3) {
            lk = 3.5f;
        } else if (bodenart == 414 && category == 1) {
            lk = 8.5f;
        } else if (bodenart == 414 && category == 2) {
            lk = 7.5f;
        } else if (bodenart == 414 && category == 3) {
            lk = 6;
        } else if (bodenart == 534 && category == 1) {
            lk = 4.5f;
        } else if (bodenart == 534 && category == 2) {
            lk = 3;
        } else if (bodenart == 534 && category == 3) {
            lk = 2;
        } else if (bodenart == 504 && category == 1) {
            lk = 4;
        } else if (bodenart == 504 && category == 2) {
            lk = 3;
        } else if (bodenart == 504 && category == 3) {
            lk = 2;
        }

        log.add("   Luftkapazität anhand der Lagerungsdichte und der Bodenart ermittelt");
        log.add("   LK -> " + lk + " l/m²");

        // correct value with Humusgehalt
        log.add("   Luftkapazität wird anhand des Humusgehaltes und der Bodenart korrigiert");
        log.add("   " + this.horizont.getAttributeLabel("Px01_Hu") + " -> " + humus + " %");

        if ((bodenart == 101 || bodenart == 121) && humus < 1) {
            lk = lk + 0;
        } else if ((bodenart == 101 || bodenart == 121) && (humus >= 1 && humus < 2)) {
            lk = lk - 1.5f;
        } else if ((bodenart == 101 || bodenart == 121) && (humus >= 2 && humus < 4)) {
            lk = lk - 1;
        } else if ((bodenart == 101 || bodenart == 121) && (humus >= 4 && humus < 8)) {
            lk = lk - 1;
        } else if ((bodenart == 101 || bodenart == 121) && (humus >= 8 && humus < 15)) {
            lk = lk + 0;
        } else if ((bodenart == 231 || bodenart == 341) && humus < 1) {
            lk = lk + 0;
        } else if ((bodenart == 231 || bodenart == 341) && (humus >= 1 && humus < 2)) {
            lk = lk + 0;
        } else if ((bodenart == 231 || bodenart == 341) && (humus >= 2 && humus < 4)) {
            lk = lk + 1;
        } else if ((bodenart == 231 || bodenart == 341) && (humus >= 4 && humus < 8)) {
            lk = lk + 2;
        } else if ((bodenart == 231 || bodenart == 341) && (humus >= 8 && humus < 15)) {
            lk = lk + 2.5f;
        } else if ((bodenart == 202 || bodenart == 212) && humus < 1) {
            lk = lk + 0;
        } else if ((bodenart == 202 || bodenart == 212) && (humus >= 1 && humus < 2)) {
            lk = lk + 0.5f;
        } else if ((bodenart == 202 || bodenart == 212) && (humus >= 2 && humus < 4)) {
            lk = lk + 1.5f;
        } else if ((bodenart == 202 || bodenart == 212) && (humus >= 4 && humus < 8)) {
            lk = lk + 2.5f;
        } else if ((bodenart == 202 || bodenart == 212) && (humus >= 8 && humus < 15)) {
            lk = lk + 5.5f;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && humus < 1) {
            lk = lk + 0;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && (humus >= 1 && humus < 2)) {
            lk = lk + 0.5f;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && (humus >= 2 && humus < 4)) {
            lk = lk + 1.5f;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && (humus >= 4 && humus < 8)) {
            lk = lk + 3;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && (humus >= 8 && humus < 15)) {
            lk = lk + 5;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && humus < 1) {
            lk = lk + 0;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && (humus >= 1 && humus < 2)) {
            lk = lk + 0.5f;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && (humus >= 2 && humus < 4)) {
            lk = lk + 1.5f;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && (humus >= 4 && humus < 8)) {
            lk = lk + 2.5f;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && (humus >= 8 && humus < 15)) {
            lk = lk + 4.5f;
        }

        if (humus > 30) {
            lk = 20;
        } else if (humus > 15 && humus <= 30) {
            if (bodenart == 101 || bodenart == 231 || bodenart == 341) {
                lk = 11;
            } else {
                lk = 6;
            }
        }

        log.add("   Korrigierte LK -> " + lk + " l/m²");

        if (skelett > 60) {
            lk = 25;
            log.add("   Skelettgehalt über 60%!");
            log.add("   Korrigierte LK -> " + lk + " l/m²");
        }

        if (lk >= 0) {
            log.add("   Berechnung anhand der Horizontmächtigkeit, des Skelettgehaltes und der FK");
            result = Math.round(maechtigkeit * 10 * (100 - skelett) / 100 * (lk / 100));
        } else {
            log.add("   FEHLER => Konnte nicht ermittelt werden! [LK<0]");
            return;
        }

        // result is not null, save value to db
        if (result != null) {
            // save to horizont
            this.horizont.setAttributeValue("Px09_LK", String.valueOf(result));
            log.add("   ERGEBNIS => " + result + " l/m²");
        }
    }

    /**
     * Wasserspeichervermögen
     */
    private void calculatePx10WSV() {
        // output
        log.add("-------------------");
        log.add("11) " + this.horizont.getAttributeLabel("Px10_WSV"));
        // check Px07_nFK && Px09_LK
        if (!isAttributeValueSet("Px07_nFK")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px07_nFK"));
            return;
        }
        if (!isAttributeValueSet("Px09_LK")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px09_LK"));
            return;
        }

        // result value
        Integer result = null;

        // get needed parameters
        float nfk = this.horizont.getAttributeValueFloat("Px07_nFK");
        float lk = this.horizont.getAttributeValueFloat("Px09_LK");

        long neigungCode = ((KeyAttribute) profil.getAttribute("hangneigung")).getId();

        if (neigungCode == 11
                || neigungCode == 12
                || neigungCode == 21
                || neigungCode == 22) {
            log.add("   Hangneigung des Profils 'eben' oder 'schwach geneigt'!");
            log.add("   -> WSV entspricht der nFK und LK");
            result = Math.round(nfk + lk);
        } else if (neigungCode < 0) {
            log.add("   Keine Hangneigungsinformation im Profil gefunden!");
            log.add("   HINWEIS => Berechnung wird genauer mit Hangneigungsinformation");
            log.add("   -> Für die LK wird einfach der Wert der nFK übernommen");
            result = Math.round(nfk);
        } else {
            log.add("   Große Hangneigung des Profils!");
            log.add("   -> WSV entspricht der nFK");
            result = Math.round(nfk);
        }

        // result is not null, save value to db
        if (result != null) {
            // save to horizont
            this.horizont.setAttributeValue("Px10_WSV", String.valueOf(result));
            log.add("   ERGEBNIS => " + result + " l/m²");
        }
    }

    /**
     * Gesamtes Porenvolumen
     */
    private void calculatePx11GPV() {
        // output
        log.add("-------------------");
        log.add("12) " + this.horizont.getAttributeLabel("Px11_GPV"));
        // check px01, px02 and px03
        if (!isAttributeValueSet("Px01_Hu")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px01_Hu"));
            return;
        }
        if (!isAttributeValueSet("Px02_Sk")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px02_Sk"));
            return;
        }
        if (!isAttributeValueSet("Px03_Ld")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px03_Ld"));
            return;
        }

        // result value
        Integer result = null;

        // get needed parameters
        float humus = this.horizont.getAttributeValueFloat("Px01_Hu");
        float skelett = this.horizont.getAttributeValueFloat("Px02_Sk");
        String dichte = this.horizont.getAttributeValue("Px03_Ld");
        int maechtigkeit = this.horizont.getThickness();

        long bodenartCode = ((KeyAttribute) this.horizont.getAttribute("bodenart")).getId();
        long bodenartLaborCode = ((KeyAttribute) this.horizont.getAttribute("bodenartlabor")).getId();
        long bodenart = -1;

        // define bodenart
        if (bodenartLaborCode >= 0) {
            log.add("   " + this.horizont.getAttributeLabel("bodenartlabor") + "  gefunden");
            log.add("   -> " + ((KeyAttribute) this.horizont.getAttribute("bodenartlabor")).getValue());
            bodenart = bodenartLaborCode;
        } else if (bodenartCode >= 0) {
            log.add("   " + this.horizont.getAttributeLabel("bodenart") + "  gefunden");
            log.add("   -> " + ((KeyAttribute) this.horizont.getAttribute("bodenart")).getValue());
            bodenart = bodenartCode;
        } else {
            log.add("   FEHLER => Angabe zur Bodenart fehlt!");
            return;
        }

        float gpv = -1;

        log.add("   " + this.horizont.getAttributeLabel("Px03_Ld") + " -> " + dichte);
        int category = 0;
        if (dichte.equalsIgnoreCase("Ld1") || dichte.equalsIgnoreCase("Ld2")) {
            category = 1;
        } else if (dichte.equalsIgnoreCase("Ld3")) {
            category = 2;
        } else if (dichte.equalsIgnoreCase("Ld4") || dichte.equalsIgnoreCase("Ld5")) {
            category = 3;
        }

        String bodenartStr = this.horizont.getApplication().getKeytable("bodenart").getTerm(bodenart, horizont.getUser().getCurrentLanguage());
        log.add("   Bodenart -> " + bodenartStr);
        if (bodenart == 101 && category == 1) {
            gpv = 42;
        } else if (bodenart == 101 && category == 2) {
            gpv = 36;
        } else if (bodenart == 101 && category == 3) {
            gpv = 30.5f;
        } else if (bodenart == 121 && category == 1) {
            gpv = 45.5f;
        } else if (bodenart == 121 && category == 2) {
            gpv = 37;
        } else if (bodenart == 121 && category == 3) {
            gpv = 32;
        } else if (bodenart == 212 && category == 1) {
            gpv = 47.5f;
        } else if (bodenart == 212 && category == 2) {
            gpv = 39;
        } else if (bodenart == 212 && category == 3) {
            gpv = 34;
        } else if (bodenart == 202 && category == 1) {
            gpv = 47.5f;
        } else if (bodenart == 202 && category == 2) {
            gpv = 40;
        } else if (bodenart == 202 && category == 3) {
            gpv = 35.5f;
        } else if (bodenart == 231 && category == 1) {
            gpv = 45.5f;
        } else if (bodenart == 231 && category == 2) {
            gpv = 38;
        } else if (bodenart == 231 && category == 3) {
            gpv = 32.5f;
        } else if (bodenart == 332 && category == 1) {
            gpv = 49.5f;
        } else if (bodenart == 332 && category == 2) {
            gpv = 42;
        } else if (bodenart == 332 && category == 3) {
            gpv = 36.5f;
        } else if (bodenart == 341 && category == 1) {
            gpv = 44;
        } else if (bodenart == 341 && category == 2) {
            gpv = 38.5f;
        } else if (bodenart == 341 && category == 3) {
            gpv = 35;
        } else if (bodenart == 313 && category == 1) {
            gpv = 49.5f;
        } else if (bodenart == 313 && category == 2) {
            gpv = 40.5f;
        } else if (bodenart == 313 && category == 3) {
            gpv = 35.5f;
        } else if (bodenart == 423 && category == 1) {
            gpv = 54;
        } else if (bodenart == 423 && category == 2) {
            gpv = 44;
        } else if (bodenart == 423 && category == 3) {
            gpv = 38;
        } else if (bodenart == 403 && category == 1) {
            gpv = 55.5f;
        } else if (bodenart == 403 && category == 2) {
            gpv = 44.5f;
        } else if (bodenart == 403 && category == 3) {
            gpv = 38.5f;
        } else if (bodenart == 414 && category == 1) {
            gpv = 50.5f;
        } else if (bodenart == 414 && category == 2) {
            gpv = 43.5f;
        } else if (bodenart == 414 && category == 3) {
            gpv = 37;
        } else if (bodenart == 534 && category == 1) {
            gpv = 61;
        } else if (bodenart == 534 && category == 2) {
            gpv = 51;
        } else if (bodenart == 534 && category == 3) {
            gpv = 44;
        } else if (bodenart == 504 && category == 1) {
            gpv = 62;
        } else if (bodenart == 504 && category == 2) {
            gpv = 52;
        } else if (bodenart == 504 && category == 3) {
            gpv = 44.5f;
        }
        log.add("   Gesamtes Porenvolumen anhand der Lagerungsdichte und der Bodenart ermittelt");
        log.add("   GPV -> " + gpv + " l/m²");

        // correct value with Humusgehalt
        log.add("   Gesamtes Porenvolumen wird anhand des Humusgehaltes und der Bodenart korrigiert");
        log.add("   " + this.horizont.getAttributeLabel("Px01_Hu") + " -> " + humus + " %");
        if ((bodenart == 101 || bodenart == 121) && humus == 0.5) {
            gpv = gpv + 0;
        } else if ((bodenart == 101 || bodenart == 121) && humus == 1.5) {
            gpv = gpv + 0;
        } else if ((bodenart == 101 || bodenart == 121) && humus == 3) {
            gpv = gpv + 2.5f;
        } else if ((bodenart == 101 || bodenart == 121) && humus == 6) {
            gpv = gpv + 6.5f;
        } else if ((bodenart == 101 || bodenart == 121) && humus == 11.5) {
            gpv = gpv + 10;
        } else if ((bodenart == 231 || bodenart == 341) && humus == 0.5) {
            gpv = gpv + 0;
        } else if ((bodenart == 231 || bodenart == 341) && humus == 1.5) {
            gpv = gpv + 1.5f;
        } else if ((bodenart == 231 || bodenart == 341) && humus == 3) {
            gpv = gpv + 4.5f;
        } else if ((bodenart == 231 || bodenart == 341) && humus == 6) {
            gpv = gpv + 10;
        } else if ((bodenart == 231 || bodenart == 341) && humus == 11.5) {
            gpv = gpv + 14;
        } else if ((bodenart == 202 || bodenart == 212) && humus == 0.5) {
            gpv = gpv + 0;
        } else if ((bodenart == 202 || bodenart == 212) && humus == 1.5) {
            gpv = gpv + 2;
        } else if ((bodenart == 202 || bodenart == 212) && humus == 3) {
            gpv = gpv + 5;
        } else if ((bodenart == 202 || bodenart == 212) && humus == 6) {
            gpv = gpv + 11.5f;
        } else if ((bodenart == 202 || bodenart == 212) && humus == 11.5) {
            gpv = gpv + 17.5f;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && humus == 0.5) {
            gpv = gpv + 0;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && humus == 1.5) {
            gpv = gpv + 3;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && humus == 3) {
            gpv = gpv + 5.5f;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && humus == 6) {
            gpv = gpv + 13;
        } else if ((bodenart == 313 || bodenart == 332 || bodenart == 403 || bodenart == 423) && humus == 11.5) {
            gpv = gpv + 18.5f;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && humus == 0.5) {
            gpv = gpv + 0;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && humus == 1.5) {
            gpv = gpv + 3;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && humus == 3) {
            gpv = gpv + 6.5f;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && humus == 6) {
            gpv = gpv + 13;
        } else if ((bodenart == 414 || bodenart == 504 || bodenart == 534) && humus == 11.5) {
            gpv = gpv + 19.5f;
        }

        if (humus > 30) {
            gpv = 80;
        } else if (humus > 15 && humus <= 30) {
            if (bodenart == 101 || bodenart == 231 || bodenart == 341) {
                gpv = 67;
            } else {
                gpv = 73;
            }
        }

        log.add("   Korrigierter GPV -> " + gpv + " l/m²");

        if (gpv >= 0) {
            log.add("   Berechnung anhand der Horizontmächtigkeit, des Skelettgehaltes und der GPV");
            result = Math.round(maechtigkeit * 10 * (100 - skelett) / 100 * (gpv / 100));
        } else {
            log.add("   FEHLER => Konnte nicht ermittelt werden! [GPV<0]");
            return;
        }

        // result is not null, save value to db
        if (result != null) {
            // save to horizont
            this.horizont.setAttributeValue("Px11_GPV", String.valueOf(result));
            log.add("   ERGEBNIS => " + result + " l/m²");
        }
    }

    /**
     * Gesättigte Wasserleitfähigkeit
     */
    private void calculatePx12kf() {
        // output
        log.add("-------------------");
        log.add("13) " + this.horizont.getAttributeLabel("Px12_kf"));
        // check px02 and px03
        if (!isAttributeValueSet("Px02_Sk")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px02_Sk"));
            return;
        }
        if (!isAttributeValueSet("Px03_Ld")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px03_Ld"));
            return;
        }

        // result value
        Integer result = null;

        // get needed parameters
        float skelett = this.horizont.getAttributeValueFloat("Px02_Sk");
        String dichte = this.horizont.getAttributeValue("Px03_Ld");

        long bodenartCode = ((KeyAttribute) this.horizont.getAttribute("bodenart")).getId();
        long bodenartLaborCode = ((KeyAttribute) this.horizont.getAttribute("bodenartlabor")).getId();
        long bodenart = -1;

        // define bodenart
        if (bodenartLaborCode >= 0) {
            log.add("   " + this.horizont.getAttributeLabel("bodenartlabor") + "  gefunden");
            log.add("   -> " + ((KeyAttribute) this.horizont.getAttribute("bodenartlabor")).getValue());
            bodenart = bodenartLaborCode;
        } else if (bodenartCode >= 0) {
            log.add("   " + this.horizont.getAttributeLabel("bodenart") + "  gefunden");
            log.add("   -> " + ((KeyAttribute) this.horizont.getAttribute("bodenart")).getValue());
            bodenart = bodenartCode;
        } else {
            log.add("   FEHLER => Angabe zur Bodenart fehlt!");
            return;
        }

        float kf = -1;

        log.add("   " + this.horizont.getAttributeLabel("Px03_Ld") + " -> " + dichte);
        int category = 0;
        if (dichte.equalsIgnoreCase("Ld1") || dichte.equalsIgnoreCase("Ld2")) {
            category = 1;
        } else if (dichte.equalsIgnoreCase("Ld3")) {
            category = 2;
        } else if (dichte.equalsIgnoreCase("Ld4") || dichte.equalsIgnoreCase("Ld5")) {
            category = 3;
        }

        String bodenartStr = this.horizont.getApplication().getKeytable("bodenart").getTerm(bodenart, horizont.getUser().getCurrentLanguage());
        log.add("   Bodenart -> " + bodenartStr);
        if (bodenart == 101 && category == 1) {
            kf = 196;
        } else if (bodenart == 101 && category == 2) {
            kf = 117;
        } else if (bodenart == 101 && category == 3) {
            kf = 61;
        } else if (bodenart == 121 && category == 1) {
            kf = 40;
        } else if (bodenart == 121 && category == 2) {
            kf = 20;
        } else if (bodenart == 121 && category == 3) {
            kf = 13;
        } else if (bodenart == 212 && category == 1) {
            kf = 32;
        } else if (bodenart == 212 && category == 2) {
            kf = 11;
        } else if (bodenart == 212 && category == 3) {
            kf = 4;
        } else if (bodenart == 202 && category == 1) {
            kf = 27;
        } else if (bodenart == 202 && category == 2) {
            kf = 8;
        } else if (bodenart == 202 && category == 3) {
            kf = 4;
        } else if (bodenart == 231 && category == 1) {
            kf = 45;
        } else if (bodenart == 231 && category == 2) {
            kf = 20;
        } else if (bodenart == 231 && category == 3) {
            kf = 11;
        } else if (bodenart == 332 && category == 1) {
            kf = 29;
        } else if (bodenart == 332 && category == 2) {
            kf = 14;
        } else if (bodenart == 332 && category == 3) {
            kf = 5;
        } else if (bodenart == 341 && category == 1) {
            kf = 60;
        } else if (bodenart == 341 && category == 2) {
            kf = 48;
        } else if (bodenart == 341 && category == 3) {
            kf = 22;
        } else if (bodenart == 313 && category == 1) {
            kf = 32;
        } else if (bodenart == 313 && category == 2) {
            kf = 16;
        } else if (bodenart == 313 && category == 3) {
            kf = 8;
        } else if (bodenart == 423 && category == 1) {
            kf = 28;
        } else if (bodenart == 423 && category == 2) {
            kf = 19;
        } else if (bodenart == 423 && category == 3) {
            kf = 7;
        } else if (bodenart == 403 && category == 1) {
            kf = 19;
        } else if (bodenart == 403 && category == 2) {
            kf = 12;
        } else if (bodenart == 403 && category == 3) {
            kf = 5;
        } else if (bodenart == 414 && category == 1) {
            kf = 15;
        } else if (bodenart == 414 && category == 2) {
            kf = 11;
        } else if (bodenart == 414 && category == 3) {
            kf = 4;
        } else if (bodenart == 534 && category == 1) {
            kf = 18;
        } else if (bodenart == 534 && category == 2) {
            kf = 6;
        } else if (bodenart == 534 && category == 3) {
            kf = 2;
        } else if (bodenart == 504 && category == 1) {
            kf = 20;
        } else if (bodenart == 504 && category == 2) {
            kf = 5;
        } else if (bodenart == 504 && category == 3) {
            kf = 1;
        }

        log.add("   Gesättigte Wasserleitfähigkeit anhand der Lagerungsdichte und der Bodenart ermittelt");
        log.add("   kf -> " + kf + " cm/d");

        // Oberboden
        long gefuegeCode = ((KeyAttribute) this.horizont.getAttribute("gefuege1")).getId();
        if ((gefuegeCode == 470 || gefuegeCode == 450) && category == 1) {
            kf = 300;
            log.add("   Lockergelagerter Horizont!");
            String gefuegeStr = this.horizont.getApplication().getKeytable("gefuege").getTerm(gefuegeCode, horizont.getUser().getCurrentLanguage()); //hier war vorher noch die Variable bodenart
            log.add("   -> " + gefuegeStr);
            log.add("   Korrigierter kf-Wert -> " + kf + " cm/d");
        }

        // Skelett
        if (skelett >= 60) {
            kf = 300;
            log.add("   Skelettgehalt über 60%!");
            log.add("   Korrigierter kf-Wert -> " + kf + " cm/d");
        }

        // Umlagerung
        boolean umlagerung = this.horizont.getAttributeValueBoolean("umlagerung");
        if (umlagerung && category == 1) {
            kf = 300;
            log.add("   Umgelagerter Boden und Ld1 oder Ld2!");
            log.add("   Korrigierter kf-Wert -> " + kf + " l/m²");
        }

        // Festgestein
        // diese if klausel macht keinen sinn mit dem gefuegeanteil wenn dieser unabhangig von der gefuegeart ist
        //int grobanteil = this.horizont.getAttributeValueInt("gefuege1_anteil");
        //String bezeichnung = this.horizont.getAttributeValue("bezeichnung");
        //if (grobanteil == 100 && bezeichnung.contains("C")) {
        //    kf = 1;
        //    log.add("   C-Horizont und Festgestein (Grobanteil=100%)!");
        //    log.add("   Korrigierter kf-Wert -> " + kf + " l/m²");
        //}

        long bodentypCode = ((KeyAttribute) profil.getAttribute("bodentyp_oe")).getId();

        if (bodentypCode >= 0
                && (bodentypCode == 2100
                || bodentypCode == 2110
                || bodentypCode == 2111
                || bodentypCode == 2112
                || bodentypCode == 2120)) {
            log.add("   Moore!");
            if (isAttributeValueSet("torf_zersetzung")) {
                String px03 = this.horizont.getAttributeValue("Px03_Ld");
                long zersetzung = ((KeyAttribute) profil.getAttribute("torf_zersetzung")).getId();
                if (("Ld1".equalsIgnoreCase(px03) || "Ld2".equalsIgnoreCase(px03))
                        && (zersetzung == 1 || zersetzung == 2)) {
                    kf = 300;
                } else if (("Ld1".equalsIgnoreCase(px03) || "Ld2".equalsIgnoreCase(px03))
                        && (zersetzung == 3)) {
                    kf = 70;
                } else if (("Ld1".equalsIgnoreCase(px03) || "Ld2".equalsIgnoreCase(px03))
                        && (zersetzung == 4 || zersetzung == 5)) {
                    kf = 25;
                } else if (("Ld3".equalsIgnoreCase(px03))
                        && (zersetzung == 1 || zersetzung == 2)) {
                    kf = 70;
                } else if (("Ld3".equalsIgnoreCase(px03))
                        && (zersetzung == 3)) {
                    kf = 25;
                } else if (("Ld3".equalsIgnoreCase(px03))
                        && (zersetzung == 4 || zersetzung == 5)) {
                    kf = 5;
                } else if (("Ld4".equalsIgnoreCase(px03) || "Ld5".equalsIgnoreCase(px03))
                        && (zersetzung == 1 || zersetzung == 2)) {
                    kf = 25;
                } else if (("Ld4".equalsIgnoreCase(px03) || "Ld5".equalsIgnoreCase(px03))
                        && (zersetzung == 3)) {
                    kf = 5;
                } else if (("Ld4".equalsIgnoreCase(px03) || "Ld5".equalsIgnoreCase(px03))
                        && (zersetzung == 4 || zersetzung == 5)) {
                    kf = 1;
                }
                log.add("   kf-Wert wird anhand des Bodentyps, der Lagerungsdichte und des Zersetzungsgrades korrigiert");
            } else {
                float humus = this.horizont.getAttributeValueFloat("Px01_Hu");
                if (humus > 30) {
                    kf = 25;
                    log.add("   kf-Wert wird anhand des Humusgehaltes korrigiert");
                }
            }
            log.add("   Korrigierter kf-Wert -> " + kf + " l/m²");
        }

        if (kf >= 0) {
            result = Math.round(kf);
        }

        // result is not null, save value to db
        if (result != null) {
            // save to horizont
            this.horizont.setAttributeValue("Px12_kf", String.valueOf(result));
            log.add("   ERGEBNIS => " + result + " cm/d");
        } else {
            log.add("   FEHLER => Konnte nicht ermittelt werden! [kf<0]");
        }
    }

    /**
     * Potentielle Kationenaustauschkapazität (KAKpot)
     */
    private void calculatePx13KAKpot() {
        // output
        log.add("-------------------");
        log.add("14) " + this.horizont.getAttributeLabel("Px13_KAKpot"));
        // check px01
        if (!isAttributeValueSet("Px01_Hu")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px01_Hu"));
            return;
        }

        // result value
        Integer result = null;

        // get needed parameters
        float humus = this.horizont.getAttributeValueFloat("Px01_Hu");
        float kakPot = -1;

        // Mineralbodenanteil
        float tongehalt = this.horizont.getAttributeValueFloat("ton");
        float schluffgehalt = this.horizont.getAttributeValueFloat("schluff");

        log.add("   Mineralbodenanteil");
        if (this.horizont.hasAttributeValue("ton") && 
                this.horizont.hasAttributeValue("schluff")) {
//        if (tongehalt >= 0 && schluffgehalt >= 0) {
            kakPot = 0.5f * tongehalt + 0.05f * schluffgehalt;
            log.add("   Ton- und Schluffgehalt gefunden");
            log.add("   KAK (pot.) -> " + kakPot + " cmol/kg");
        } else {
            log.add("   Keinen Ton- oder Schluffgehalt gefunden, pot. KAK wird anhand Bodenart ermittelt");
            log.add("   HINWEIS => Die Berechnung ist genauer mit Ton- und Schluffgehalt!");

            long bodenartCode = ((KeyAttribute) this.horizont.getAttribute("bodenart")).getId();
            long bodenartLaborCode = ((KeyAttribute) this.horizont.getAttribute("bodenartlabor")).getId();
            long bodenart = -1;

            // define bodenart
            if (bodenartLaborCode >= 0) {
                log.add("   " + this.horizont.getAttributeLabel("bodenartlabor") + "  gefunden");
                log.add("   -> " + ((KeyAttribute) this.horizont.getAttribute("bodenartlabor")).getValue());
                bodenart = bodenartLaborCode;
            } else if (bodenartCode >= 0) {
                log.add("   " + this.horizont.getAttributeLabel("bodenart") + "  gefunden");
                log.add("   -> " + ((KeyAttribute) this.horizont.getAttribute("bodenart")).getValue());
                bodenart = bodenartCode;
            } else {
                log.add("   FEHLER => Angabe zur Bodenart fehlt!");
                return;
            }

            if (bodenart == 101) {
                kakPot = 3;
            } else if (bodenart == 121) {
                kakPot = 4;
            } else if (bodenart == 212) {
                kakPot = 8;
            } else if (bodenart == 202) {
                kakPot = 10;
            } else if (bodenart == 231) {
                kakPot = 7;
            } else if (bodenart == 332) {
                kakPot = 14;
            } else if (bodenart == 341) {
                kakPot = 9;
            } else if (bodenart == 313) {
                kakPot = 13;
            } else if (bodenart == 423) {
                kakPot = 18;
            } else if (bodenart == 403) {
                kakPot = 18;
            } else if (bodenart == 414) {
                kakPot = 18;
            } else if (bodenart == 534) {
                kakPot = 27;
            } else if (bodenart == 504) {
                kakPot = 30;
            }
            log.add("   KAK (pot.) -> " + kakPot + " cmol/kg");
        }

        // Humuszuschlag
        Float kakHum = humus * 2;
        log.add("   Humusanteil");
        log.add("   KAK-Humus (pot.) -> " + kakHum + " cmol/kg");

        kakPot = kakPot + kakHum;
        log.add("   Summe -> " + kakPot + " cmol/kg");

        if (kakPot >= 0) {
            result = Math.round(kakPot);
        } else {
            log.add("   FEHLER => Konnte nicht ermittelt werden! [KAK (pot.)<0]");
            return;
        }

        // result is not null, save value to db
        if (result != null) {
            // save to horizont
            this.horizont.setAttributeValue("Px13_KAKpot", String.valueOf(result));
            log.add("   ERGEBNIS => " + result + " cmol/kg");
        }
    }

    /**
     * Effektive Kationenaustauschkapazität (KAKeff)
     */
    private void calculatePx14KAKeff() {
        // output
        log.add("-------------------");
        log.add("15) " + this.horizont.getAttributeLabel("Px14_KAKeff"));
        // check Px13_KAKpot, ph
        if (!isAttributeValueSet("Px13_KAKpot")) {
            log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.horizont.getAttributeLabel("Px13_KAKpot"));
            return;
        }
        if (!isAttributeValueSet("ph_wert")) {
            log.add("   FEHLER => pH-Wert fehlt!");
            return;
        }

        // result value
        Integer result = null;

        // get needed parameters
        float kakPot = this.horizont.getAttributeValueFloat("Px13_KAKpot");
        float ph = this.horizont.getAttributeValueFloat("ph_wert");

        float factor = -1;

        if (ph < 0) {
            log.add("   pH-Wert -> " + ph);
            log.add("   FEHLER => Ungültiger pH-Wert in der Datenbank gefunden!");
            return;
        } else if (ph >= 7) {
            factor = 1;
        } else if (ph < 7 && ph >= 6) {
            factor = 0.9f;
        } else if (ph < 6 && ph >= 5) {
            factor = 0.7f;
        } else if (ph < 5 && ph >= 4) {
            factor = 0.5f;
        } else if (ph < 4 && ph >= 3) {
            factor = 0.3f;
        } else if (ph < 3) {
            factor = 0.25f;
        }
        log.add("   Eff. KAK ergibt sich aus der Umrechnung der pot. KAK");
        log.add("   Umrechnungsfaktor -> " + factor);

        result = Math.round(kakPot * factor);

        // result is not null, save value to db
        if (result != null) {
            // save to horizont
            this.horizont.setAttributeValue("Px14_KAKeff", String.valueOf(result));
            log.add("   ERGEBNIS => " + result + " cmol/kg");
        }
    }

    /**
     * Basensättigung
     */
    private void calculatePx15Mb() {
        // output
        log.add("-------------------");
        log.add("16) " + this.horizont.getAttributeLabel("Px15_Mb"));
        // check ph
        if (!isAttributeValueSet("ph_wert")) {
            log.add("   FEHLER => pH-Wert fehlt!");
            return;
        }

        // result value
        Integer result = null;

        // get needed parameters
        float ph = this.horizont.getAttributeValueFloat("ph_wert");

        if (ph < 0) {
            log.add("   pH-Wert -> " + ph);
            log.add("   FEHLER => Ungültiger pH-Wert in der Datenbank gefunden!");
            return;
        } else if (ph >= 7.5f) {
            result = 100;
        } else if (ph >= 7.0f && ph < 7.5f) {
            result = 95;
        } else if (ph >= 6.5f && ph < 7.0f) {
            result = 90;
        } else if (ph >= 6.0f && ph < 6.5f) {
            result = 80;
        } else if (ph >= 5.5f && ph < 6.0f) {
            result = 70;
        } else if (ph >= 5.1f && ph < 5.5f) {
            result = 60;
        } else if (ph >= 4.8f && ph < 5.1f) {
            result = 50;
        } else if (ph >= 4.5f && ph < 4.8f) {
            result = 40;
        } else if (ph >= 4.2f && ph < 4.5f) {
            result = 30;
        } else if (ph >= 3.8f && ph < 4.2f) {
            result = 20;
        } else if (ph >= 3.5f && ph < 3.8f) {
            result = 10;
        } else if (ph >= 3.3f && ph < 3.5f) {
            result = 5;
        } else if (ph >= 3.0f && ph < 3.3f) {
            result = 2;
        } else if (ph < 3.0f) {
            result = 0;
        }
        log.add("   Ermittlung anhand des pH-Werts");
        // result is not null, save value to db
        if (result != null) {
            // save to horizont
            this.horizont.setAttributeValue("Px15_Mb", String.valueOf(result));
            log.add("   ERGEBNIS => " + result + " %");
        }
    }

    public ArrayList<String> getLog() {
        return log.getLog();
    }

}
