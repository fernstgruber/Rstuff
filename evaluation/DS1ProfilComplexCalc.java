package at.grid.sepp3.core.evaluation;

import at.grid.cms.attribute.KeyAttribute;
import at.grid.cms.element.CmsElementSummary;
import at.grid.sepp3.core.app.SeppLogger;
import at.grid.sepp3.core.element.CmsHorizont;
import at.grid.sepp3.core.element.CmsProfil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class calculates the complex parameters for a given CmsProfil element.
 *
 * @author pk
 */
public class DS1ProfilComplexCalc {

  // ========================================================================
  //  MEMBERS
  // ========================================================================
  /**
   * The profil used for calculation
   */
  private CmsProfil profil;
  /**
   * List of horizonte
   */
  ArrayList<CmsHorizont> horizonte = new ArrayList<CmsHorizont>();
//  /**
//   * Calculation log
//   */
//  ArrayList<String> log = new ArrayList<String>();
  /**
   * Calculation log
   */
  ArrayList<String> log = new ArrayList<String>();

  // ========================================================================
  //  CONSTRUCTOR
  // ========================================================================
  public DS1ProfilComplexCalc(CmsProfil prf) {
    this.profil = prf;
    log.clear();
    List<CmsElementSummary> eles = this.profil.getRelationAttribute("horizonte").getElements();
    for (CmsElementSummary ele : eles) {
      this.horizonte.add((CmsHorizont) ele.getElement());
    }
  }

//##############################################################################
//  HELPER METHODS
//##############################################################################
  public boolean isAttributeValueSet(String att) {
    String value = this.profil.getAttributeValue(att);
    if (value != null && !"".equalsIgnoreCase(value)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Very basic check to assert non-empty float attributes (assuming negative
   * value corresponds to NoData)
   *
   * @param att
   * @return
   */
  public boolean checkFloatAttribute(String att) {
    float value = this.profil.getAttributeValueFloat(att);
    if (value >= 0) {
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

    log.add("Profil '" + profil.getTitle() + "' -> Berechnung der komplexen Parameter gestartet!");

    this.calculateSx12We();
    this.calculateSx10kfmin();
    this.calculateSx01FBges();
    this.calculateSx02TMges();
    this.calculateSx03HMges();
    this.calculateSx04nFKges();
    this.calculateSx04anFKwe();
    this.calculateSx05FKges();
    this.calculateSx05aFKwe();
    this.calculateSx06LKges();
    this.calculateSx06aLKwe();
    this.calculateSx07LKoben();
    this.calculateSx08WSVges();
    this.calculateSx09WSVwe();
    this.calculateSx11kfave();
    this.calculateSx13KAKeff();
    this.calculateSx14KAKWe();
    this.calculateSx15MbWe();

    log.add("Profil '" + profil.getTitle() + "' -> Berechnung der komplexen Parameter fertig!");
  }

  /**
   * Physiologische Gründigkeit (must be calculated first!)
   */
  private void calculateSx12We() {
    //  output
    log.add("1) " + this.profil.getAttributeLabel("Sx12_We"));

    Integer result = null;
    // check gruendigkeit_wert; Dieser soll falls vorhand immer als Wurzeltiefe verwendet werden! nur wenn nicht vorhanden die berechnungen durchfuehren
    if (isAttributeValueSet("gruendigkeit_wert") && this.profil.getAttributeValueFloat("gruendigkeit_wert")> 0) {
      float gruendigkeit = this.profil.getAttributeValueFloat("gruendigkeit_wert");
      this.profil.setAttributeValue("Sx12_We", String.valueOf(gruendigkeit));
      log.add("   Gruendigkeit von Kartierer wird übernommen! -> " + this.profil.getAttributeValueFloat("Sx12_We")+" cm");
      return;
    }

    if (!isAttributeValueSet("gruendigkeit_wert")) {
             // Grundwassereinfluss
    log.add("   Gruendigkeit von Kartierer nicht vorhanden und muss berechnet werden! " );
    boolean grundwasser = this.profil.getAttributeValueBoolean("grundwasser");
    long bodentypCode = ((KeyAttribute) this.profil.getAttribute("bodentyp_oe")).getId();
    Long[] landnutzung = this.profil.getAttributeMultikeyValueIds("landnutzung");
    List<Long> list = null;
    if (landnutzung != null) {
      list = new ArrayList<Long>(Arrays.asList(landnutzung));
    }

    if (grundwasser) {
      log.add("   Grundwassereinfluss! Es wird geprüft, ob ein 'Gr' oder 'G2' Horizont vorhanden ist");
      for (CmsHorizont h : this.horizonte) {
        String bezeichnung = h.getAttributeValue("bezeichnung");
        if ("Gr".equalsIgnoreCase(bezeichnung)) {
          result = h.getUpperLimit() - 10;
          log.add("   Gr-Horizont gefunden! Obergrenze dieses Horizont (" + h.getUpperLimit() + " cm) - 10cm ");
        } else if ("G2".equalsIgnoreCase(bezeichnung)) {
          result = h.getUpperLimit() - 10;
          log.add("   G2-Horizont gefunden! Obergrenze dieses Horizont (" + h.getUpperLimit() + " cm) - 10cm ");
        }
      }
    } else if (bodentypCode >= 0 && (bodentypCode == 2100 || bodentypCode == 2110
            || bodentypCode == 2111 || bodentypCode == 2112 || bodentypCode == 2120)) {
      // Moorböden
      if (list != null && list.contains(10L)) {
        log.add("   Moorboden mit Ackernutzung!");
        result = 60;
      } else {
        log.add("   Moorboden ohne Ackernutzung!");
        result = 40;
      }
    } else {
      // loop through horizons and see if ther is one with skelett=100%
      for (CmsHorizont h : this.horizonte) {
        if (h.getAttributeValueFloat("Px02_Sk") == 100) {
          // skelettgehalt = 100%
          Integer up = h.getUpperLimit();
          if (up >= 0 && up <= 100) {
            log.add("   Horizont '" + h.getAttributeValue("bezeichnung") + "' mit 100% Skelettgehalt!");
            log.add("   Obergrenze -> up");
            result = up;
          }
        }
      }
    }

    if (result == null) {
      // Standardtiefe
      log.add("   Es wird die Standardtiefe des Profils verwendet (100cm)");
      result = 100;
    }

    if (result != null) {
      // save to profil
      this.profil.setAttributeValue("Sx12_We", String.valueOf(result));
      log.add("   ERGEBNIS => " + result + " cm");
    }
     }
  }

  /**
   * Minimaler kf-Wert im gesamten Profil (muss hier berechnet werden, da er
   * z.B. in Sx07 verwendet wird)
   */
  private void calculateSx10kfmin() {
    //  output
    log.add("-------------------");
    log.add("2) " + this.profil.getAttributeLabel("Sx10_kfmin"));
    // result value
    Integer result = null;

    float minKF = Float.MAX_VALUE;

    for (CmsHorizont h : this.horizonte) {
      float px12 = h.getAttributeValueFloat("Px12_kf");
      if (px12 < 0) {
        log.add("   FEHLER => kf-Wert nicht für alle Horizonte vorhanden. Siehe '" + h.getAttributeValue("bezeichnung") + "'");
        return;
      }
      if (px12 < minKF) {
        minKF = px12;
      }
    }

    if (minKF != Float.MAX_VALUE) {
      log.add("   Minimaler kf-Wert konnte ermittelt werden");
      result = Math.round(minKF);
    }

    if (result != null) {
      // save to profil
      this.profil.setAttributeValue("Sx10_kfmin", String.valueOf(result));
      log.add("   ERGEBNIS => " + result + " cm/d");
    }
  }

  /**
   * Feinbodenmenge im gesamten Profil
   */
  private void calculateSx01FBges() {
    //  output
    log.add("-------------------");
    log.add("3) " + this.profil.getAttributeLabel("Sx01_FBges"));

    // result value
    Integer result = null;

    float sum = 0;
    // Aufsummieren von Px04
    for (CmsHorizont h : this.horizonte) {
      float px04 = h.getAttributeValueFloat("Px04_FB");
      if (px04 < 0) {
        log.add("   FEHLER => Feinbodenmenge nicht für alle Horizonte vorhanden -> siehe '" + h.getAttributeValue("bezeichnung") + "'");
        return;
      }
      sum += px04;
    }
    result = Math.round(sum);

    if (result != null) {
      // save to profil
      this.profil.setAttributeValue("Sx01_FBges", String.valueOf(result));
      log.add("   ERGEBNIS => " + result + " kg/m²");
    }
  }

  /**
   * Tonmenge im gesamten Profil
   */
  private void calculateSx02TMges() {
    //  output
    log.add("-------------------");
    log.add("4) " + this.profil.getAttributeLabel("Sx02_TMges"));

    // result value
    Integer result = null;

    float sum = 0;
    // Aufsummieren von Px05
    for (CmsHorizont h : this.horizonte) {
      float px05 = h.getAttributeValueFloat("Px05_TM");
      if (px05 < 0) {
        log.add("   FEHLER => Tonmenge nicht für alle Horizonte vorhanden -> siehe '" + h.getAttributeValue("bezeichnung") + "'");
        return;
      }
      sum += px05;

    }
    result = Math.round(sum);

    if (result != null) {
      // save to profil
      this.profil.setAttributeValue("Sx02_TMges", String.valueOf(result));
      log.add("   ERGEBNIS => " + result + " kg/m²");
    }
  }

  /**
   * Humusmenge im gesamten Profil
   */
  private void calculateSx03HMges() {
    //  output
    log.add("-------------------");
    log.add("5) " + this.profil.getAttributeLabel("Sx03_HMges"));
    // result value
    Integer result = null;

    float sum = 0;
    // Aufsummieren von Px06
    for (CmsHorizont h : this.horizonte) {
      float px06 = h.getAttributeValueFloat("Px06_HM");
      if (px06 < 0) {
        log.add("   FEHLER => Humusmenge nicht für alle Horizonte vorhanden -> siehe '" + h.getAttributeValue("bezeichnung") + "'");
        return;
      }
      sum += px06;

    }
    result = Math.round(sum);

    if (result != null) {
      // save to profil
      this.profil.setAttributeValue("Sx03_HMges", String.valueOf(result));
      log.add("   ERGEBNIS => " + result + " kg/m²");
    }
  }

  /**
   * nFK im gesamten Profil
   */
  private void calculateSx04nFKges() {
    //  output
    log.add("-------------------");
    log.add("6) " + this.profil.getAttributeLabel("Sx04_nFKges"));
    // result value
    Integer result = null;

    float sum = 0;
    // Aufsummieren von Px07
    for (CmsHorizont h : this.horizonte) {
      float px07 = h.getAttributeValueFloat("Px07_nFK");
      if (px07 < 0) {
        log.add("   FEHLER => Nuztbare Feldkapazität nicht für alle Horizonte vorhanden -> siehe '" + h.getAttributeValue("bezeichnung") + "'");
        return;
      }
      sum += px07;
    }
    result = Math.round(sum);

    if (result != null) {
      // save to profil
      this.profil.setAttributeValue("Sx04_nFKges", String.valueOf(result));
      log.add("   ERGEBNIS => " + result + " l/m²");

    }
  }

  /**
   * nFK im Wurzelraum
   */
  private void calculateSx04anFKwe() {
    //  output
    log.add("-------------------");
    log.add("7) " + this.profil.getAttributeLabel("Sx04a_nFKwe"));
    // check Sx12_We
    if (!isAttributeValueSet("Sx12_We")) {
      log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.profil.getAttributeLabel("Sx12_We"));
      return;
    }

    // result value
    Integer result = null;
    float sum = 0;

    // get WE
    float wurzelTiefe = this.profil.getAttributeValueFloat("Sx12_We");
    log.add("   Wurzeltiefe des Profils : " + wurzelTiefe + " cm");

    for (CmsHorizont h : this.horizonte) {
      int tiefe = h.getAttributeValueInt("tiefe");
      String bezeichnung = h.getAttributeValue("bezeichnung");
      int upperLimit = h.getUpperLimit();

      if (tiefe < 0 && tiefe > 100) {
        log.add("   FEHLER => Tiefe (=Untergrenze) nicht für alle Horizonte vorhanden -> siehe '" + bezeichnung + "'");
        return;
      }

      if ((tiefe <= wurzelTiefe) || (tiefe > wurzelTiefe
              && (upperLimit >= 0 && upperLimit < wurzelTiefe))) {
        // check needed values
        float px07 = h.getAttributeValueFloat("Px07_nFK");
        if (px07 < 0) {
          log.add("   FEHLER => Nuztbare Feldkapazität für Horizont '" + bezeichnung + "' nicht vorhanden! Summe für den Wurzelraum kann nicht berechnet werden.");
          return;
        }
        if (tiefe <= wurzelTiefe) {
          log.add("   '" + bezeichnung + "' : Tiefe (" + tiefe + "cm) ist kleiner als Wurzeltiefe");
          log.add("   -> nFK des Horizontes fließt zur Gänze in die nFK-Summe für den Wurzelraum");
          sum += px07;
        } else if (tiefe > wurzelTiefe
                && (upperLimit >= 0 && upperLimit < wurzelTiefe)) {
          log.add("   '" + bezeichnung + "' : Obergrenze (" + upperLimit + " cm) ist kleiner und Tiefe (" + tiefe + " cm) ist größer als Wurzeltiefe");
          log.add("   -> nFK des Horizontes fließt nur anteilmäßig in die nFK-Summe für den Wurzelraum");
          float part = wurzelTiefe - upperLimit;
          float percent = part / h.getThickness();
          log.add("   -> " + part + " cm des Horizontes über Wurzeltiefe, entspricht einem Anteil von " + percent + " %");
          sum += (percent * px07);
        }
      }
    }

    result = Math.round(sum);
    if (result != null) {
      // save to profil
      this.profil.setAttributeValue("Sx04a_nFKwe", String.valueOf(result));
      log.add("   ERGEBNIS => " + result + " l/m²");
    }
  }

  /**
   * FK im gesamten Profil
   */
  private void calculateSx05FKges() {
    //  output
    log.add("-------------------");
    log.add("8) " + this.profil.getAttributeLabel("Sx05_FKges"));
    // result value
    Integer result = null;

    float sum = 0;
    // Aufsummieren von Px08
    for (CmsHorizont h : this.horizonte) {
      float px08 = h.getAttributeValueFloat("Px08_FK");
      if (px08 < 0) {
        log.add("   FEHLER => Feldkapazität nicht für alle Horizonte vorhanden -> siehe '" + h.getAttributeValue("bezeichnung") + "'");
        return;
      }
      sum += px08;

    }
    result = Math.round(sum);

    if (result != null) {
      // save to profil
      this.profil.setAttributeValue("Sx05_FKges", String.valueOf(result));
      log.add("   ERGEBNIS => " + result + " l/m²");
    }
  }

  /**
   * FK im Wurzelraum
   */
  private void calculateSx05aFKwe() {
    //  output
    log.add("-------------------");
    log.add("8) " + this.profil.getAttributeLabel("Sx05a_FKwe"));
    // check Sx12_We
    if (!isAttributeValueSet("Sx12_We")) {
      log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.profil.getAttributeLabel("Sx12_We"));
      return;
    }

    // result value
    Integer result = null;
    float sum = 0;

    // get WE
    float wurzelTiefe = this.profil.getAttributeValueFloat("Sx12_We");
    log.add("   Wurzeltiefe des Profils : " + wurzelTiefe + " cm");

    for (CmsHorizont h : this.horizonte) {
      int tiefe = h.getAttributeValueInt("tiefe");
      String bezeichnung = h.getAttributeValue("bezeichnung");
      int upperLimit = h.getUpperLimit();

      if (tiefe < 0 && tiefe > 100) {
        log.add("   FEHLER => Tiefe (=Untergrenze) nicht für alle Horizonte vorhanden -> siehe '" + bezeichnung + "'");
        return;
      }

      if ((tiefe <= wurzelTiefe) || (tiefe > wurzelTiefe
              && (upperLimit >= 0 && upperLimit < wurzelTiefe))) {
        // check needed values
        float px08 = h.getAttributeValueFloat("Px08_FK");
        if (px08 < 0) {
          log.add("   FEHLER => Feldkapazität für Horizont '" + bezeichnung + "' nicht vorhanden! Summe für den Wurzelraum kann nicht berechnet werden.");
          return;
        }
        if (tiefe <= wurzelTiefe) {
          log.add("   '" + bezeichnung + "' : Tiefe (" + tiefe + "cm) ist kleiner als Wurzeltiefe");
          log.add("   -> FK des Horizontes fließt zur Gänze in die FK-Summe für den Wurzelraum");
          sum += px08;
        } else if (tiefe > wurzelTiefe
                && (upperLimit >= 0 && upperLimit < wurzelTiefe)) {
          log.add("   '" + bezeichnung + "' : Obergrenze (" + upperLimit + " cm) ist kleiner und Tiefe (" + tiefe + " cm) ist größer als Wurzeltiefe");
          log.add("   -> FK des Horizontes fließt nur anteilmäßig in die FK-Summe für den Wurzelraum");
          float part = wurzelTiefe - upperLimit;
          float percent = part / h.getThickness();
          log.add("   -> " + part + " cm des Horizontes über Wurzeltiefe, entspricht einem Anteil von " + percent + " %");
          sum += (percent * px08);
        }
      }
    }
    result = Math.round(sum);

    if (result != null) {
      // save to profil
      this.profil.setAttributeValue("Sx05a_FKwe", String.valueOf(result));
      log.add("   ERGEBNIS => " + result + " l/m²");
    }
  }

  /**
   * Luftkapazität im gesamten Profil
   */
  private void calculateSx06LKges() {
    //  output
    log.add("-------------------");
    log.add("9) " + this.profil.getAttributeLabel("Sx06_LKges"));
    // result value
    Integer result = null;

    float sum = 0;

    for (CmsHorizont h : this.horizonte) {
      // Aufsummieren von Px09
      float px09 = h.getAttributeValueFloat("Px09_LK");
      if (px09 < 0) {
        log.add("   FEHLER => Luftkapazität nicht für alle Horizonten vorhanden -> siehe '" + h.getAttributeValue("bezeichnung") + "'");
        return;
      }
      sum += px09;

    }
    result = Math.round(sum);

    if (result != null) {
      // save to profil
      this.profil.setAttributeValue("Sx06_LKges", String.valueOf(result));
      log.add("   ERGEBNIS => " + result + " l/m²");
    }
  }

  /**
   * Luftkapazität im eff. Wurzelraum
   */
  private void calculateSx06aLKwe() {
    //  output
    log.add("-------------------");
    log.add("10) " + this.profil.getAttributeLabel("Sx06a_LKwe"));
    // check Sx12_We
    if (!isAttributeValueSet("Sx12_We")) {
      log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.profil.getAttributeLabel("Sx12_We"));
      return;
    }

    // result value
    Integer result = null;
    float sum = 0;

    // get WE
    float wurzelTiefe = this.profil.getAttributeValueFloat("Sx12_We");
    log.add("   Wurzeltiefe des Profils : " + wurzelTiefe + " cm");

    for (CmsHorizont h : this.horizonte) {
      int tiefe = h.getAttributeValueInt("tiefe");
      String bezeichnung = h.getAttributeValue("bezeichnung");
      int upperLimit = h.getUpperLimit();

      if (tiefe < 0 && tiefe > 100) {
        log.add("   FEHLER => Tiefe (=Untergrenze) nicht für alle Horizonte vorhanden -> siehe '" + bezeichnung + "'");
        return;
      }

      if ((tiefe <= wurzelTiefe) || (tiefe > wurzelTiefe
              && (upperLimit >= 0 && upperLimit < wurzelTiefe))) {
        // check needed values
        float px09 = h.getAttributeValueFloat("Px09_LK");
        if (px09 < 0) {
          log.add("   FEHLER => Luftkapazität für Horizont '" + bezeichnung + "' nicht vorhanden! Summe für den Wurzelraum kann nicht berechnet werden.");
          return;
        }
        if (tiefe <= wurzelTiefe) {
          log.add("   '" + bezeichnung + "' : Tiefe (" + tiefe + "cm) ist kleiner als Wurzeltiefe");
          log.add("   -> LK des Horizontes fließt zur Gänze in die LK-Summe für den Wurzelraum");
          sum += px09;
        } else if (tiefe > wurzelTiefe
                && (upperLimit >= 0 && upperLimit < wurzelTiefe)) {
          log.add("   '" + bezeichnung + "' : Obergrenze (" + upperLimit + " cm) ist kleiner und Tiefe (" + tiefe + " cm) ist größer als Wurzeltiefe");
          log.add("   -> LK des Horizontes fließt nur anteilmäßig in die FK-Summe für den Wurzelraum");
          float part = wurzelTiefe - upperLimit;
          float percent = part / h.getThickness();
          log.add("   -> " + part + " cm des Horizontes über Wurzeltiefe, entspricht einem Anteil von " + percent + " %");
          sum += (percent * px09);
        }
      }
    }
    result = Math.round(sum);

    if (result != null) {
      // save to profil
      this.profil.setAttributeValue("Sx06a_LKwe", String.valueOf(result));
      log.add("   ERGEBNIS => " + result + " l/m²");
    }
  }

  /**
   * Luftkapazität oberhalb eines „Stauhorizontes“
   */
  private void calculateSx07LKoben() {
    //  output
    log.add("-------------------");
    log.add("11) " + this.profil.getAttributeLabel("Sx07_LKoben"));

    // result value
    Integer result = null;

    // Ermitteln des "Stauers"
    long bodentyp = ((KeyAttribute) this.profil.getAttribute("bodentyp_oe")).getId();
    Float flurabstand = this.profil.getAttributeValueFloat("flurabstand");
    log.add("   Ermittlung der \"Stautiefe\"...");
    // Diese Variable speichert die "Stautiefe", danach wird die LK für alle darüber liegenden Horizonte berechnet
    int stau = -1;
    if ((flurabstand > 0 && flurabstand <= 100)
            || (bodentyp == 1820 || bodentyp == 1821 || bodentyp == 1822 // Gleye
            || (bodentyp >= 2000 && bodentyp <= 2033))) {
      log.add("   Grundwasser -> Flurabstand wurde gesetzt oder Boden ist dem Gleye zuzuteilen");
      log.add("   Es wird geprüft, ob ein 'Gr' oder 'G2' Horizont vorhanden ist");
      // 1) Grundwassereinfluss
      for (CmsHorizont h : this.horizonte) {
        String bezeichnung = h.getAttributeValue("bezeichnung");

        if ("Gr".equalsIgnoreCase(bezeichnung) || "G2".equalsIgnoreCase(bezeichnung)) {
          int limit = h.getUpperLimit();
          if (limit >= 0) {
            log.add("   " + bezeichnung + "-Horizont gefunden! Obergrenze dieses Horizont (" + limit + " cm) wird als \"Stautiefe\" definiert");
            stau = limit;
          }
        }
      }
    } else if (bodentyp >= 1900 && bodentyp <= 1950) {
      log.add("   Stauwasser -> Es wird geprüft, ob ein 'Sd' oder 'S-' Horizont vorhanden ist");
      // 2) Stauwassereinfluss
      for (CmsHorizont h : this.horizonte) {
        String bezeichnung = h.getAttributeValue("bezeichnung");
        if (bezeichnung.startsWith("S")) { // Sd, S- nicht differenziert
          int limit = h.getAttributeValueInt("tiefe");
          if (limit >= 0) {
            log.add("   " + bezeichnung + "-Horizont gefunden! Untergrenze dieses Horizont (" + limit + " cm) wird als \"Stautiefe\" definiert");
            stau = limit;
          }
        }
      }
    } else if (bodentyp >= 2100 && bodentyp <= 2120) {
      log.add("   Moorboden -> Es wird zuerst geprüft, ob der Flurabstand gesetzt wurde");
      // 3) Moore
      if (flurabstand >= 0 && flurabstand <= 100) {
        log.add("   GW-Flurabstand bekannt, diese Tiefe wird als \"Stautiefe\" definiert");
        // Grundwasserstand bekannt ... umrechnen in Zentimeter
        stau = flurabstand.intValue() * 100;
      } else {
        log.add("   GW-Flurabstand nicht gesetzt. Überprüfen, ob ein Gr-Horizont vorhanden ist");
        for (CmsHorizont h : this.horizonte) {
          if (h.getAttributeValue("bezeichnung").startsWith("Gr")) {
            int limit = h.getUpperLimit();
            if (limit >= 0) {
              log.add("   Gr-Horizont gefunden! Obergrenze dieses Horizont (" + limit + " cm) wird als \"Stautiefe\" definiert");
              stau = limit;
            }
          }
        }
      }
    } else {
      log.add("   Gibt es Horizont(e) mit einem kf-Wert von 0 oder 1? Wenn ja, den obersten als Stauhorizont definieren");
      // 4) Horizont mit kf-Wert 1 oder 0 (obersten Horizont)
      ArrayList<CmsHorizont> kfList = new ArrayList<CmsHorizont>();
      for (CmsHorizont h : this.horizonte) {
        if (h.getAttributeValueFloat("Px12_kf") == 0
                || h.getAttributeValueFloat("Px12_kf") == 1) {
          kfList.add(h);
        }
      }
      log.add("   " + kfList.size() + " Horizont(e) mit einem kf-Wert von 0 oder 1 gefunden");
      // finde den obersten Horizont (mit minimaler Tiefe)
      int min = 100;
      if (!kfList.isEmpty()) {
        CmsHorizont topH = null;
        for (CmsHorizont h : kfList) {
          int depth = h.getAttributeValueInt("tiefe");
          if (depth >= 0 && depth <= min) {
            stau = h.getUpperLimit();
            min = depth;
            topH = h;
          }
        }
        if (topH == null) {
          log.add("   \"Stautiefe\" nicht gesetzt, kein Horizont definiert");
        } else {
          log.add("   \"Stautiefe\" auf Obergrenze von " + topH.getAttributeValue("bezeichnung") + "-Horizont gesetzt -> " + stau + " cm");
        }
      } else {
        log.add("   \"Stautiefe\" nicht gesetzt");
      }

    }

    // überprüfe 'stau' auf ungültigen Wert
    if (stau <= 0 || stau > 100) {
      log.add("   Ungültiger Wert für \"Stautiefe\", wird auf Standardtiefe gesetzt -> 100 cm");
      stau = 100;
    }

    if (stau == 100) {
      log.add("   Horizont mit minimalen kf-Wert wird gesucht");
      // wenn stau=100, verwenden von Sx10
      if (!isAttributeValueSet("Sx10_kfmin")) {
        log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.profil.getAttributeLabel("Sx10_kfmin"));
        return;
      }

      float kfmin = this.profil.getAttributeValueFloat("Sx10_kfmin");

      // Horizont mit minimalen kf-Wert finden
      int untergrenze = -1;
      for (CmsHorizont h : this.horizonte) {
        if (kfmin == h.getAttributeValueFloat("Px12_kf")) {
          // Untergrenze des gesuchten (stauenden) Horizontes
          untergrenze = h.getAttributeValueInt("tiefe");
          log.add("   \"Stautiefe\" auf Untegrenze von " + h.getAttributeValue("bezeichnung") + "-Horizont (" + untergrenze + " cm) gesetzt");
        }
      }
      // Ausummieren
      log.add("   LK der darüber liegenden Horizonte werden aufsummiert.");
      float sumPx09 = 0;
      for (CmsHorizont h : this.horizonte) {
        if (h.getAttributeValueFloat("tiefe") >= 0 && h.getAttributeValueFloat("tiefe") <= untergrenze) {
          float px09 = h.getAttributeValueFloat("Px09_LK");
          if (px09 < 0) {
            log.add("   FEHLER => Luftkapazität nicht für alle Horizonte vorhanden -> siehe '" + h.getAttributeValue("bezeichnung") + "'");
            return;
          }
          sumPx09 += px09;
        }
      }
      // Ergebnis
      result = Math.round(sumPx09);
    } else {
      // stau!=100, nur für diesen Bereich berechnen
      log.add("   \"Stautiefe\" -> " + stau + " cm");
      log.add("   LK der darüber liegenden Horizonte werden aufsummiert.");
      ArrayList<CmsHorizont> aboveStau = new ArrayList<CmsHorizont>();
      for (CmsHorizont h : this.horizonte) {
        if (h.getAttributeValueInt("tiefe") >= 0 && h.getAttributeValueInt("tiefe") <= stau) {
          // Horizont liegt darüber, hinzufügen
          aboveStau.add(h);
        }
      }

      // Ausummieren
      float sumPx09 = 0;
      for (CmsHorizont h : aboveStau) {
        float px09 = h.getAttributeValueFloat("Px09_LK");
        if (px09 < 0) {
          log.add("   FEHLER => Luftkapazität nicht für alle Horizonte vorhanden -> siehe '" + h.getAttributeValue("bezeichnung") + "'");
          return;
        }
        sumPx09 += px09;
      }
      // Ergebnis
      result = Math.round(sumPx09);
    }

    if (result != null) {
      // save to profil
      this.profil.setAttributeValue("Sx07_LKoben", String.valueOf(result));
      log.add("   ERGEBNIS => " + result + " l/m²");
    }
  }

  /**
   * Wasserspeichervermögen im gesamten Profil
   */
  private void calculateSx08WSVges() {
    //  output
    log.add("-------------------");
    log.add("12) " + this.profil.getAttributeLabel("Sx08_WSVges"));
    // result value
    Integer result = null;

    float sum = 0;
    // Aufsummieren von Px10
    for (CmsHorizont h : this.horizonte) {
      float px10 = h.getAttributeValueFloat("Px10_WSV");
      if (px10 < 0) {
        log.add("   FEHLER => WSW nicht für alle Horizonten vorhanden -> siehe '" + h.getAttributeValue("bezeichnung") + "'");
        return;
      }
      sum += px10;
    }

    result = Math.round(sum);

    if (result != null) {
      // save to profil
      this.profil.setAttributeValue("Sx08_WSVges", String.valueOf(result));
      log.add("   ERGEBNIS => " + result + " l/m²");
    }
  }

  /**
   * Wasserspeichervermögen im Wurzelraum
   */
  private void calculateSx09WSVwe() {
    //  output
    log.add("-------------------");
    log.add("13) " + this.profil.getAttributeLabel("Sx09_WSVwe"));
    // check Sx12_We
    if (!isAttributeValueSet("Sx12_We")) {
      log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.profil.getAttributeLabel("Sx12_We"));
      return;
    }
    // result value
    Integer result = null;
    float sum = 0;

    // get We
    float wurzelTiefe = this.profil.getAttributeValueFloat("Sx12_We");
    log.add("   Wurzeltiefe des Profils : " + wurzelTiefe + " cm");

    for (CmsHorizont h : this.horizonte) {
      int tiefe = h.getAttributeValueInt("tiefe");
      String bezeichnung = h.getAttributeValue("bezeichnung");
      int upperLimit = h.getUpperLimit();

      if (tiefe < 0 && tiefe > 100) {
        log.add("   FEHLER => Tiefe (=Untergrenze) nicht für alle Horizonte vorhanden -> siehe '" + bezeichnung + "'");
        return;
      }

      if ((tiefe <= wurzelTiefe) || (tiefe > wurzelTiefe
              && (upperLimit >= 0 && upperLimit < wurzelTiefe))) {
        // check needed values
        float px10 = h.getAttributeValueFloat("Px10_WSV");
        if (px10 < 0) {
          log.add("   FEHLER => Wasserspeichervermögen für Horizont '" + bezeichnung + "' nicht vorhanden! Summe für den Wurzelraum kann nicht berechnet werden.");
          return;
        }
        if (tiefe <= wurzelTiefe) {
          log.add("   '" + bezeichnung + "' : Tiefe (" + tiefe + "cm) ist kleiner als Wurzeltiefe");
          log.add("   -> WSV des Horizontes fließt zur Gänze in die WSV-Summe für den Wurzelraum");
          sum += px10;
        } else if (tiefe > wurzelTiefe
                && (upperLimit >= 0 && upperLimit < wurzelTiefe)) {
          log.add("   '" + bezeichnung + "' : Obergrenze (" + upperLimit + " cm) ist kleiner und Tiefe (" + tiefe + " cm) ist größer als Wurzeltiefe");
          log.add("   -> WSV des Horizontes fließt nur anteilmäßig in die WSV-Summe für den Wurzelraum");
          float part = wurzelTiefe - upperLimit;
          float percent = part / h.getThickness();
          log.add("   -> " + part + " cm des Horizontes über Wurzeltiefe, entspricht einem Anteil von " + percent + " %");
          sum += (percent * px10);
        }
      }
    }

    result = Math.round(sum);
    if (result != null) {
      // save to profil
      this.profil.setAttributeValue("Sx09_WSVwe", String.valueOf(result));
      log.add("   ERGEBNIS => " + result + " l/m²");
    }
  }

  /**
   * Durchschnittlicher kf-Wert im gesamten Profil
   */
  private void calculateSx11kfave() {
    //  output
    log.add("-------------------");
    log.add("14) " + this.profil.getAttributeLabel("Sx11_kfave"));
    // result value
    Integer result = null;

    float sum = 0;
    float depth=0;

    for (CmsHorizont h : this.horizonte) {
      int thickness = h.getThickness();
      float px12 = h.getAttributeValueFloat("Px12_kf");

      if (px12 < 0) {
        log.add("   FEHLER => kf-Wert nicht für alle Horizonte vorhanden -> siehe '" + h.getAttributeValue("bezeichnung") + "'-Horizont");
        return;
      }
      sum += (thickness / px12);
      depth += thickness;
    }

    // als gesamte Profilmächtigkeit wird immer 100 angenommen
    if (sum > 0.0) {
      log.add("   FRÜHER:Es wird immer 100 cm als Profilmächtigkeit angenommen!");
      log.add("   JETZT: HARMONISCHES MITTEL BEZOGEN AUF DIE PROFILTIEFE");
      result = Math.round(depth / sum);
      // result = Math.round(depth / sum);
    }
    if (result != null) {
      this.profil.setAttributeValue("Sx11_kfave", String.valueOf(result));
      log.add("   ERGEBNIS => " + result + " cm/d");
    }
  }

  /**
   * Effektive Kationenaustauschkapazität im gesamten Profil
   */
  private void calculateSx13KAKeff() {
    //  output
    log.add("-------------------");
    log.add("15) " + this.profil.getAttributeLabel("Sx13_KAKges"));
    // result value
    Integer result = null;

    float sum = 0;

    for (CmsHorizont h : this.horizonte) {
      // eff. KAK wird für jedes Horizont berechnet wenn die Daten vorhanden sind
      float px14 = h.getAttributeValueFloat("Px14_KAKeff");
      float px04 = h.getAttributeValueFloat("Px04_FB");

      if (px14 < 0) {
        log.add("   FEHLER => Eff. KAK nicht für alle Horizonte vorhanden -> siehe '" + h.getAttributeValue("bezeichnung") + "'-Horizont");
        return;
      }
      if (px04 < 0) {
        log.add("   FEHLER => Feinbodenmenge nicht für alle Horizonte vorhanden -> siehe '" + h.getAttributeValue("bezeichnung") + "'-Horizont");
        return;
      }

      // Aufsummieren
      if (px14 >= 0 && px04 >= 0) {
        sum = sum + (px14 * px04);
      }
    }
    result = Math.round(sum);

    if (result != null) {
      // save to profil
      this.profil.setAttributeValue("Sx13_KAKges", String.valueOf(result));
      log.add("   ERGEBNIS => " + result + " cmol/m²");
    }
  }

  /**
   * Eff. KAK bezogen auf den Feinboden des eff. Wurzelraums
   */
  private void calculateSx14KAKWe() {
    //  output
    log.add("-------------------");
    log.add("16) " + this.profil.getAttributeLabel("Sx14_KAKWe"));
    // check Sx12_We
    if (!isAttributeValueSet("Sx12_We")) {
      log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.profil.getAttributeLabel("Sx12_We"));
      return;
    }

    Integer result = null;
    float sum = 0;

    // get We
    float wurzelTiefe = this.profil.getAttributeValueFloat("Sx12_We");
    log.add("   Wurzeltiefe des Profils : " + wurzelTiefe + " cm");

    for (CmsHorizont h : this.horizonte) {
      int tiefe = h.getAttributeValueInt("tiefe");
      String bezeichnung = h.getAttributeValue("bezeichnung");
      int upperLimit = h.getUpperLimit();

      if (tiefe < 0 && tiefe > 100) {
        log.add("   FEHLER => Tiefe (=Untergrenze) nicht für alle Horizonte vorhanden -> siehe '" + bezeichnung + "'");
        return;
      }

      if ((tiefe <= wurzelTiefe) || (tiefe > wurzelTiefe
              && (upperLimit >= 0 && upperLimit < wurzelTiefe))) {
        // check needed values
        float px04 = h.getAttributeValueFloat("Px04_FB");
        float px14 = h.getAttributeValueFloat("Px14_KAKeff");
        if (px04 < 0) {
          log.add("   FEHLER => Feinbodenmenge für Horizont '" + bezeichnung + "' nicht vorhanden! Summe für den Wurzelraum kann nicht berechnet werden.");
          return;
        }
        if (px14 < 0) {
          log.add("   FEHLER => Eff. KAK für Horizont '" + bezeichnung + "' nicht vorhanden! Summe für den Wurzelraum kann nicht berechnet werden.");
          return;
        }
        if (tiefe <= wurzelTiefe) {
          log.add("   '" + bezeichnung + "' : Tiefe (" + tiefe + "cm) ist kleiner als Wurzeltiefe");
          log.add("   -> Eff. KAK des Horizontes fließt zur Gänze in die eff. KAK-Summe für den Wurzelraum");
          sum += (px14 * px04);
        } else if (tiefe > wurzelTiefe
                && (upperLimit >= 0 && upperLimit < wurzelTiefe)) {
          log.add("   '" + bezeichnung + "' : Obergrenze (" + upperLimit + " cm) ist kleiner und Tiefe (" + tiefe + " cm) ist größer als Wurzeltiefe");
          log.add("   -> Eff. KAK des Horizontes fließt nur anteilmäßig in die eff. KAK-Summe für den Wurzelraum");
          float part = wurzelTiefe - upperLimit;
          float percent = part / h.getThickness();
          log.add("   -> " + part + " cm des Horizontes über Wurzeltiefe, entspricht einem Anteil von " + percent + " %");
          sum += percent * (px14 * px04);
        }
      }
    }
    result = Math.round(sum);

    if (result != null) {
      // save to profil
      this.profil.setAttributeValue("Sx14_KAKWe", String.valueOf(result));
      log.add("   ERGEBNIS => " + result + " cmol/m²");
    }
  }

  /**
   * Vorrat austauschbarer basischer Kationen im effektiven Wurzelraum
   */
  private void calculateSx15MbWe() {
    //  output
    log.add("-------------------");
    log.add("17) " + this.profil.getAttributeLabel("Sx15_MbWe"));
    // check Sx12_We
    if (!isAttributeValueSet("Sx12_We")) {
      log.add("   FEHLER => Komplexer Parameter fehlt! -> " + this.profil.getAttributeLabel("Sx12_We"));
      return;
    }

    // result value
    Integer result = null;
    float sum = 0;

    // get We
    float wurzelTiefe = this.profil.getAttributeValueFloat("Sx12_We");
    log.add("   Wurzeltiefe des Profils : " + wurzelTiefe + " cm");
    log.add("   Anhand dessen pH-Wert wird für jedes Horizont über diese Wurzeltiefe der Umrechnungsfaktor ermittelt");

    for (CmsHorizont h : this.horizonte) {
      //Umrechnungsfaktor berechnen
      String bezeichnung = h.getAttributeValue("bezeichnung");
      int tiefe = h.getAttributeValueInt("tiefe");
      int upperLimit = h.getUpperLimit();

      if (tiefe < 0 && tiefe > 100) {
        log.add("   FEHLER => Tiefe (=Untergrenze) nicht für alle Horizonte vorhanden -> siehe '" + bezeichnung + "'");
        return;
      }

      if ((tiefe <= wurzelTiefe) || (tiefe > wurzelTiefe
              && (upperLimit >= 0 && upperLimit < wurzelTiefe))) {
        log.add("   '" + bezeichnung + "'- Horizont");
        // check needed values
        float px13 = h.getAttributeValueFloat("Px13_KAKpot");
        float px15 = h.getAttributeValueFloat("Px15_Mb");
        float px04 = h.getAttributeValueFloat("Px04_FB");
        if (px04 < 0) {
        log.add("   FEHLER => Feinbodenmenge nicht für alle Horizonte vorhanden -> siehe '" + h.getAttributeValue("bezeichnung") + "'");
        return;
        }
        if (px13 < 0) {
          log.add("   FEHLER => Pot. KAK für Horizont '" + bezeichnung + "' nicht vorhanden! Summe für den Wurzelraum kann nicht berechnet werden.");
          return;
        }
        if (px15 < 0) {
          log.add("   FEHLER => Basensättigung für Horizont '" + bezeichnung + "' nicht vorhanden! Summe für den Wurzelraum kann nicht berechnet werden.");
          return;
        }

        float ph = h.getAttributeValueFloat("ph_wert");
        if (ph <= 0) {
          log.add("   FEHLER => pH-Wert fehlt für dieses Horizont! Summe für den Wurzelraum kann nicht berechnet werden ");
          return;
        }

        float factor = -1;
        if (ph >= 7.5) {
          factor = 1;
        } else if (ph >= 7.0f && ph < 7.5f) {
          factor = 0.95f;
        } else if (ph >= 6.5f && ph < 7.0f) {
          factor = 0.9f;
        } else if (ph >= 6.0f && ph < 6.5f) {
          factor = 0.75f;
        } else if (ph >= 5.5f && ph < 6.0f) {
          factor = 0.6f;
        } else if (ph >= 5.0f && ph < 5.5f) {
          factor = 0.45f;
        } else if (ph >= 4.5f && ph < 5.0f) {
          factor = 0.3f;
        } else if (ph >= 4.0f && ph < 4.5f) {
          factor = 0.2f;
        } else if (ph >= 3.5f && ph < 4.0f) {
          factor = 0.1f;
        } else if (ph < 3.5f) {
          factor = 0.02f;
        }
        log.add("   Umrechnungsfaktor -> " + factor);

        if (tiefe <= wurzelTiefe) {
          log.add("   '" + bezeichnung + "' : Tiefe (" + tiefe + "cm) ist kleiner als Wurzeltiefe");
          log.add("   -> Horizont fließt zur Gänze in die Summe für den Wurzelraum");
         // sum = sum + (px15 * px13 * factor); ALTE FORMEL, ersetzt durch:
         sum = sum + (px04 * px13 * factor);
        } else if (tiefe > wurzelTiefe
                && (upperLimit >= 0 && upperLimit < wurzelTiefe)) {
          log.add("   '" + bezeichnung + "' : Obergrenze (" + upperLimit + " cm) ist kleiner und Tiefe (" + tiefe + " cm) ist größer als Wurzeltiefe");
          log.add("   -> Horizontes fließt nur anteilmäßig in die Summe für den Wurzelraum");
          float part = wurzelTiefe - upperLimit;
          float percent = part / h.getThickness();
          log.add("   -> " + part + " cm des Horizontes über Wurzeltiefe, entspricht einem Anteil von " + percent + " %");
         // sum = sum + percent * (px15 * px13 * factor); alte Formel, Basensättigung ersetzt durch Feinbodenmenge
         sum = sum + percent * (px04 * px13 * factor); 
        }
      }
    }

    result = Math.round(sum);
    if (result != null) {
      // save to profil
      this.profil.setAttributeValue("Sx15_MbWe", String.valueOf(result));
      log.add("   ERGEBNIS => " + result + " cmol/m²");
    }
  }

//  public ArrayList<String> getLog() {
//    return log;
//  }
  
  public ArrayList<String> getLog() {
    return log;
  }
  

  public String getLogAsString() {
    StringBuilder sb = new StringBuilder();
    for (String s : this.log) {
      sb.append(s).append("\n");
    }
    return sb.toString();
  }    
}
