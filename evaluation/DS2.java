package at.grid.sepp3.core.evaluation;

import at.grid.cms.attribute.KeyAttribute;
import at.grid.sepp3.core.app.SeppProject;
import at.grid.sepp3.core.element.CmsBodenkarte;
import at.grid.util.Util;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.openide.util.Exceptions;


/**
 * @author pk
 */
public class DS2 {

//##############################################################################
//  CLASS FIELDS
//##############################################################################
  private CmsBodenkarte bk;
  private Map<String, String> resultMap = new HashMap<String, String>();

//##############################################################################
//  CONSTRUCTOR
//##############################################################################
  public DS2(CmsBodenkarte ele) {
    this.bk = ele;
  }

//##############################################################################
//  HELPER METHODS
//##############################################################################
  /**
   * Very basic check to assert non-empty attributes
   * @param att
   * @return
   */
  public boolean checkAttribute(String att) {
    String value = this.bk.getAttributeValue(att);
    if (value != null && !"".equalsIgnoreCase(value)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Very basic check to assert non-empty float attributes (assuming negative value corresponds to NoData)
   * @param att
   * @return
   */
  public boolean checkFloatAttribute(String att) {
    float value = this.bk.getAttributeValueFloat(att);
    if (value >= 0) {
      return true;
    } else {
      return false;
    }
  }

  public Map<String, String> getResultMap() {
    return resultMap;
  }

//##############################################################################
//  SOIL EVALUATION METHODS
//##############################################################################
  public void calcAll() {

    try {
      this.calculate1A2Dry();
    } catch (Exception ex) {
      this.resultMap.put("Leben_Tr", "SCHWERWIEGENDER FEHLER");
      Exceptions.printStackTrace(ex);
    }
    try {
      this.calculate1A2Humid();
    } catch (Exception ex) {
      this.resultMap.put("Leben_Fe", "SCHWERWIEGENDER FEHLER");
      Exceptions.printStackTrace(ex);
    }
    try {
      this.calculate1A3();
    } catch (Exception ex) {
      this.resultMap.put("Leben_Org", "SCHWERWIEGENDER FEHLER");
      Exceptions.printStackTrace(ex);
    }
    try {
      this.calculate1A4();
    } catch (Exception ex) {
      this.resultMap.put("Leben_Kult", "SCHWERWIEGENDER FEHLER");
      Exceptions.printStackTrace(ex);
    }
    try {
      this.calculate1C1();
    } catch (Exception ex) {
      this.resultMap.put("Retention", "SCHWERWIEGENDER FEHLER");
      Exceptions.printStackTrace(ex);
    }

    try {
      this.calculate1C3();
    } catch (Exception ex) {
      this.resultMap.put("GWneu", "SCHWERWIEGENDER FEHLER");
      Exceptions.printStackTrace(ex);
    }

    try {
      this.calculate1C5();
    } catch (Exception ex) {
      this.resultMap.put("CO2_Senke", "SCHWERWIEGENDER FEHLER");
      Exceptions.printStackTrace(ex);
    }
    try {
      this.calculate1D1();
    } catch (Exception ex) {
      this.resultMap.put("FiltPuff", "SCHWERWIEGENDER FEHLER");
      Exceptions.printStackTrace(ex);
    }
    try {
      this.calculate1D2();
    } catch (Exception ex) {
      this.resultMap.put("Transform", "SCHWERWIEGENDER FEHLER");
      Exceptions.printStackTrace(ex);
    }

    try {
      this.calculate1D4();
    } catch (Exception ex) {
      this.resultMap.put("FiltPuff_Nit", "SCHWERWIEGENDER FEHLER");
      Exceptions.printStackTrace(ex);
    }

  }

  public void calculate1A2Dry() throws Exception {
    Integer result = null;

    // get attributes
    long bodenartOB1 = ((KeyAttribute) this.bk.getAttribute("Bodenart_OB_1")).getId();
    long bodenartOB2 = ((KeyAttribute) this.bk.getAttribute("Bodenart_OB_2")).getId();
    long bodenartUB1 = ((KeyAttribute) this.bk.getAttribute("Bodenart_UB_1")).getId();
    long bodenartUB2 = ((KeyAttribute) this.bk.getAttribute("Bodenart_UB_2")).getId();

    float py01 = this.bk.getAttributeValueFloat("Py01_OB_cm");
    float py02 = this.bk.getAttributeValueFloat("Py02_UB_cm");
    long py03min = ((KeyAttribute) this.bk.getAttribute("Py03_Hu_min")).getId();
    long py03max = ((KeyAttribute) this.bk.getAttribute("Py03_Hu_max")).getId();
    long py04OBmin = ((KeyAttribute) this.bk.getAttribute("Py04_OB_Sk_min")).getId();
    long py04OBmax = ((KeyAttribute) this.bk.getAttribute("Py04_OB_Sk_max")).getId();
    long py04OB = -1;
    long py04UBmin = ((KeyAttribute) this.bk.getAttribute("Py04_UB_Sk_min")).getId();
    long py04UBmax = ((KeyAttribute) this.bk.getAttribute("Py04_UB_Sk_max")).getId();
    long py04UB = -1;
    long py08min = ((KeyAttribute) this.bk.getAttribute("Py08_Feuchte_min")).getId();
    long py08max = ((KeyAttribute) this.bk.getAttribute("Py08_Feuchte_max")).getId();
    long py09min = ((KeyAttribute) this.bk.getAttribute("Py09_WSV_min")).getId();
    long py09max = ((KeyAttribute) this.bk.getAttribute("Py09_WSV_max")).getId();

    // 1a) Abschätzung des relevanten Porenvolumens auf Basis der Bodenart

    // check
    if (bodenartOB1 < 0 && bodenartOB2 < 0) {
      this.resultMap.put("Leben_Tr", "FEHLER : Es muss mind. eine Bodenart für den Oberboden definiert werden");
      return;
    }
    if (bodenartUB1 < 0 && bodenartUB2 < 0) {
      this.resultMap.put("Leben_Tr", "FEHLER : Es muss mind. eine Bodenart für den Unterboden definiert werden");
      return;
    }

    // Berechnung nFK für OB und UB
    double nfkOB1 = -1;
    if (bodenartOB1 == 202) {
      nfkOB1 = 14;
    } else if (bodenartOB1 == 504) {
      nfkOB1 = 14.5;
    } else if (bodenartOB1 == 534) {
      nfkOB1 = 14.5;
    } else if (bodenartOB1 == 101) {
      nfkOB1 = 14.5;
    } else if (bodenartOB1 == 341) {
      nfkOB1 = 15;
    } else if (bodenartOB1 == 414) {
      nfkOB1 = 15.5;
    } else if (bodenartOB1 == 403) {
      nfkOB1 = 15.5;
    } else if (bodenartOB1 == 423) {
      nfkOB1 = 16;
    } else if (bodenartOB1 == 313) {
      nfkOB1 = 17;
    } else if (bodenartOB1 == 332) {
      nfkOB1 = 19;
    } else if (bodenartOB1 == 231) {
      nfkOB1 = 21;
    } else if (bodenartOB1 == 121) {
      nfkOB1 = 23;
    } else if (bodenartOB1 == 212) {
      nfkOB1 = 23.5;
    }

    double nfkOB2 = -1;
    if (bodenartOB2 == 202) {
      nfkOB2 = 14;
    } else if (bodenartOB2 == 504) {
      nfkOB2 = 14.5;
    } else if (bodenartOB2 == 534) {
      nfkOB2 = 14.5;
    } else if (bodenartOB2 == 101) {
      nfkOB2 = 14.5;
    } else if (bodenartOB2 == 341) {
      nfkOB2 = 15;
    } else if (bodenartOB2 == 414) {
      nfkOB2 = 15.5;
    } else if (bodenartOB2 == 403) {
      nfkOB2 = 15.5;
    } else if (bodenartOB2 == 423) {
      nfkOB2 = 16;
    } else if (bodenartOB2 == 313) {
      nfkOB2 = 17;
    } else if (bodenartOB2 == 332) {
      nfkOB2 = 19;
    } else if (bodenartOB2 == 231) {
      nfkOB2 = 21;
    } else if (bodenartOB2 == 121) {
      nfkOB2 = 23;
    } else if (bodenartOB2 == 212) {
      nfkOB2 = 23.5;
    }

    double nfkUB1 = -1;
    if (bodenartUB1 == 202) {
      nfkUB1 = 14;
    } else if (bodenartUB1 == 504) {
      nfkUB1 = 14.5;
    } else if (bodenartUB1 == 534) {
      nfkUB1 = 14.5;
    } else if (bodenartUB1 == 101) {
      nfkUB1 = 14.5;
    } else if (bodenartUB1 == 341) {
      nfkUB1 = 15;
    } else if (bodenartUB1 == 414) {
      nfkUB1 = 15.5;
    } else if (bodenartUB1 == 403) {
      nfkUB1 = 15.5;
    } else if (bodenartUB1 == 423) {
      nfkUB1 = 16;
    } else if (bodenartUB1 == 313) {
      nfkUB1 = 17;
    } else if (bodenartUB1 == 332) {
      nfkUB1 = 19;
    } else if (bodenartUB1 == 231) {
      nfkUB1 = 21;
    } else if (bodenartUB1 == 121) {
      nfkUB1 = 23;
    } else if (bodenartUB1 == 212) {
      nfkUB1 = 23.5;
    }

    double nfkUB2 = -1;
    if (bodenartUB2 == 202) {
      nfkUB2 = 14;
    } else if (bodenartUB2 == 504) {
      nfkUB2 = 14.5;
    } else if (bodenartUB2 == 534) {
      nfkUB2 = 14.5;
    } else if (bodenartUB2 == 101) {
      nfkUB2 = 14.5;
    } else if (bodenartUB2 == 341) {
      nfkUB2 = 15;
    } else if (bodenartUB2 == 414) {
      nfkUB2 = 15.5;
    } else if (bodenartUB2 == 403) {
      nfkUB2 = 15.5;
    } else if (bodenartUB2 == 423) {
      nfkUB2 = 16;
    } else if (bodenartUB2 == 313) {
      nfkUB2 = 17;
    } else if (bodenartUB2 == 332) {
      nfkUB2 = 19;
    } else if (bodenartUB2 == 231) {
      nfkUB2 = 21;
    } else if (bodenartUB2 == 121) {
      nfkUB2 = 23;
    } else if (bodenartUB2 == 212) {
      nfkUB2 = 23.5;
    }

    // nFK für OB und UB (Durchschnitt)
    double nfkOben = -1;
    if (nfkOB1 >= 0 && nfkOB2 >= 0) {
      nfkOben = (nfkOB1 + nfkOB2) / 2;
    } else if (nfkOB1 >= 0 && nfkOB2 < 0) {
      nfkOben = nfkOB1;
    } else if (nfkOB1 < 0 && nfkOB2 >= 0) {
      nfkOben = nfkOB2;
    }
    if (nfkOben < 0) {
      this.resultMap.put("Leben_Tr", "FEHLER : Die nFK konnte für den Oberboden nicht berechnet werden");
      return;
    }

    double nfkUnten = -1;
    if (nfkUB1 >= 0 && nfkUB2 >= 0) {
      nfkUnten = (nfkUB1 + nfkUB2) / 2;
    } else if (nfkUB1 >= 0 && nfkUB2 < 0) {
      nfkUnten = nfkUB1;
    } else if (nfkUB1 < 0 && nfkUB2 >= 0) {
      nfkUnten = nfkUB2;
    }
    if (nfkUnten < 0) {
      this.resultMap.put("Leben_Tr", "FEHLER : Die nFK konnte für den Unterboden nicht berechnet werden");
      return;
    }

    // 1b) Korrektur von nfkOben nach dem Humusgehalt (Py03)
    // Humus MIN
    double humusMin = -1;
    double nfkMin = -1;
    if (py03min == 0) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = 0;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 0;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 0;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 0;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 0;
      }
    } else if (py03min == 1) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = 0.5;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 0.5;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 0.5;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 0.5;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 1;
      }
    } else if (py03min == 2) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = 1;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 1;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 1;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 1.5;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 2.5;
      }
    } else if (py03min == 3) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = 3;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 3;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 3.5;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 4;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 5.5;
      }
    } else if (py03min == 4) {
      nfkMin = 37;
    } else if (py03min == 5) {
      nfkMin = 50;
    }
    // Humus MAX
    double humusMax = -1;
    double nfkMax = -1;
    if (py03max == 0) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = 0;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 0;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 0;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 0;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 0;
      }
    } else if (py03max == 1) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = 0.5;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 0.5;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 0.5;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 0.5;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 1;
      }
    } else if (py03max == 2) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = 1;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 1;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 1;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 1.5;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 2.5;
      }
    } else if (py03max == 3) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = 3;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 3;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 3.5;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 4;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 5.5;
      }
    } else if (py03max == 4) {
      nfkMax = 37;
    } else if (py03max == 5) {
      nfkMax = 50;
    }

    // Korrektur Humus
    if (humusMin >= 0 && humusMax >= 0) {
      // Mittelwert beider Korrekturwerte, da sie beide gesetzt wurden
      double humusCorr = (humusMin + humusMax) / 2;
      nfkOben += humusCorr;
    } else if (humusMin >= 0 && humusMax < 0) {
      // Mittelwert aus Korrektur und Ausnahme (nfkMax), falls gesetzt
      double nfkObenMod = nfkOben + humusMin;
      if (nfkMax > 0) {
        nfkOben = (nfkObenMod + nfkMax) / 2;
      } else {
        nfkOben += humusMin;
      }
    } else if (humusMin < 0 && humusMax >= 0) {
      // Mittelwert aus Korrektur und Ausnahme (nfkMin), falls gesetzt
      double nfkObenMod = nfkOben + humusMax;
      if (nfkMin > 0) {
        nfkOben = (nfkObenMod + nfkMin) / 2;
      } else {
        nfkOben += humusMax;
      }
    } else if (humusMin < 0 && humusMax < 0 && nfkMin > 0 && nfkMax > 0) {
      nfkOben = (nfkMin + nfkMax) / 2;
    }


    // 1c) Berechnung der nutzbaren Feldkapazität in [l/m²]

    if (py01 < 0) {
      this.resultMap.put("Leben_Tr", "FEHLER : Mächtigkeit des Oberbodens nicht vorhanden");
      return;
    }

    // Skelettgehalt Oberboden
    if (py04OBmin >= 0 && py04OBmax >= 0) {
      py04OB = (py04OBmin + py04OBmax) / 2;
    } else if (py04OBmin >= 0 && py04OBmax < 0) {
      py04OB = py04OBmin;
    } else if (py04OBmin < 0 && py04OBmax >= 0) {
      py04OB = py04OBmax;
    }
    if (py04OB < 0) {
      this.resultMap.put("Leben_Tr", "FEHLER : Skelettgehalt des Oberbodens nicht vorhanden");
      return;
    }

    // Skelettgehalt Unterboden
    if (py04UBmin >= 0 && py04UBmax >= 0) {
      py04UB = (py04UBmin + py04UBmax) / 2;
    } else if (py04UBmin >= 0 && py04UBmax < 0) {
      py04UB = py04UBmin;
    } else if (py04UBmin < 0 && py04UBmax >= 0) {
      py04UB = py04UBmax;
    }
    if (py04UB < 0) {
      this.resultMap.put("Leben_Tr", "FEHLER : Skelettgehalt des Unterbodens nicht vorhanden");
      return;
    }

    double nFKsum = -1;
    if (py01 >= 0 && py04OB >= 0 && nfkOben >= 0 && py02 >= 0 && py04UB >= 0 && nfkUnten >= 0) {
      nFKsum = (py01 * 10 * (1 - py04OB / 100)) * (nfkOben / 100)
              + (py02 * 10 * (1 - py04UB / 100)) * (nfkUnten / 100);
    }

    // 2. Schritt: Bewertung des Potenzials als Trockenstandort

    long bodentypkurz = ((KeyAttribute) this.bk.getAttribute("Py07_Typ")).getId();
    if (bodentypkurz == 2 || bodentypkurz == 3) {
      result = 4;
    } else if (bodentypkurz == 1) {
      result = 5;
    } else {
      // ckeck Feuchtestufe
      if (py08min < 0 && py08max < 0) {
        this.resultMap.put("Leben_Tr", "FEHLER : Es fehlt die Angabe zur Feuchtestufe");
        return;
      }
      // Feuchtestufe (wird für beide weitere Bewertungen benötigt (mit nFK und mit WSV)
      // Feuchtestufe min
      int resultPy08min = -1;
      if (py08min == 1) {
        resultPy08min = 1;
      } else if (py08min == 2) {
        resultPy08min = 2;
      } else if (py08min == 3 || py08min == 4) {
        resultPy08min = 3;
      } else if (py08min == 5 || py08min == 8 || py08min == 9) {
        resultPy08min = 4;
      } else if (py08min == 6 || py08min == 7) {
        resultPy08min = 5;
      }
      // Feuchtestufe max
      int resultPy08max = -1;
      if (py08max == 1) {
        resultPy08max = 1;
      } else if (py08max == 2) {
        resultPy08max = 2;
      } else if (py08max == 3 || py08max == 4) {
        resultPy08max = 3;
      } else if (py08max == 5 || py08max == 8 || py08max == 9) {
        resultPy08max = 4;
      } else if (py08max == 6 || py08max == 7) {
        resultPy08max = 5;
      }
      // Mittelwert Feuchtestufe
      double resultPy08 = -1;
      if (resultPy08min > 0 && resultPy08max > 0) {
        resultPy08 = (resultPy08min + resultPy08max) / 2;
      } else if (resultPy08min > 0 && resultPy08max <= 0) {
        resultPy08 = resultPy08min;
      } else if (resultPy08min <= 0 && resultPy08max > 0) {
        resultPy08 = resultPy08max;
      }
      if (resultPy08 < 0) {
        this.resultMap.put("Leben_Tr", "FEHLER: Bewertung nach der Feuchtestufe konnte nicht durchgeführt werden");
        return;
      }

      if (nFKsum >= 0) {
        // Bewertung mit nFK und Feuchtestufe

        // nFK
        int resultnFK = -1;
        if (nFKsum <= 30) {
          resultnFK = 1;
        } else if (nFKsum > 30 && nFKsum <= 60) {
          resultnFK = 2;
        } else if (nFKsum > 60 && nFKsum <= 140) {
          resultnFK = 3;
        } else if (nFKsum > 140 && nFKsum <= 220) {
          resultnFK = 4;
        } else if (nFKsum > 220) {
          resultnFK = 5;
        }
        if (resultnFK < 0) {
          this.resultMap.put("Leben_Tr", "FEHLER: Bewertung nach der nFK konnte nicht durchgeführt werden");
          return;
        }
        // Ergebnis
        Double bew = Math.floor((resultnFK + resultPy08) / 2);
        result = bew.intValue();

      } else {
        // Bewertung mit WSV und Feuchtestufe

        // check WSV
        if (py09min < 0 && py09max < 0) {
          this.resultMap.put("Leben_Tr", "FEHLER : Es fehlt die Angabe zur Speicherkraft");
          return;
        }
        // WSV min
        int resultPy09min = -1;
        if (py09min == 5) {
          resultPy09min = 1;
        } else if (py09min == 4) {
          resultPy09min = 2;
        } else if (py09min == 3) {
          resultPy09min = 3;
        } else if (py09min == 2) {
          resultPy09min = 4;
        } else if (py09min == 1) {
          resultPy09min = 5;
        }
        // WSV max
        int resultPy09max = -1;
        if (py09max == 5) {
          resultPy09max = 1;
        } else if (py09max == 4) {
          resultPy09max = 2;
        } else if (py09max == 3) {
          resultPy09max = 3;
        } else if (py09max == 2) {
          resultPy09max = 4;
        } else if (py09max == 1) {
          resultPy09max = 5;
        }
        // Mittelwert WSV
        double resultPy09 = -1;
        if (resultPy09min > 0 && resultPy09max > 0) {
          resultPy09 = (resultPy09min + resultPy09max) / 2;
        } else if (resultPy09min > 0 && resultPy09max <= 0) {
          resultPy09 = resultPy09min;
        } else if (resultPy09min <= 0 && resultPy09max > 0) {
          resultPy09 = resultPy09max;
        }
        if (resultPy09 < 0) {
          this.resultMap.put("Leben_Tr", "FEHLER: Bewertung nach der Feuchtestufe konnte nicht durchgeführt werden");
          return;
        }

        // Ergebnis
        Double bew = Math.floor((resultPy08 + resultPy09) / 2);
        result = bew.intValue();
      }
    }

    if (result != null) {
      this.bk.setAttributeValue("Leben_Tr", String.valueOf(result));
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': 1A.2 - Dry saved with value '" + result + "'");
      this.resultMap.put("Leben_Tr", String.valueOf(result));
    } else {
      this.resultMap.put("Leben_Tr", "FEHLER : Konnte nicht ermittelt werden");
    }
  }

  public void calculate1A2Humid() throws Exception {
    Integer result = null;

    // get attributes
    long bodenartOB1 = ((KeyAttribute) this.bk.getAttribute("Bodenart_OB_1")).getId();
    long bodenartOB2 = ((KeyAttribute) this.bk.getAttribute("Bodenart_OB_2")).getId();
    long bodenartUB1 = ((KeyAttribute) this.bk.getAttribute("Bodenart_UB_1")).getId();
    long bodenartUB2 = ((KeyAttribute) this.bk.getAttribute("Bodenart_UB_2")).getId();

    float py01 = this.bk.getAttributeValueFloat("Py01_OB_cm");
    float py02 = this.bk.getAttributeValueFloat("Py02_UB_cm");
    long py03min = ((KeyAttribute) this.bk.getAttribute("Py03_Hu_min")).getId();
    long py03max = ((KeyAttribute) this.bk.getAttribute("Py03_Hu_max")).getId();
    long py04OBmin = ((KeyAttribute) this.bk.getAttribute("Py04_OB_Sk_min")).getId();
    long py04OBmax = ((KeyAttribute) this.bk.getAttribute("Py04_OB_Sk_max")).getId();
    long py04OB = -1;
    long py04UBmin = ((KeyAttribute) this.bk.getAttribute("Py04_UB_Sk_min")).getId();
    long py04UBmax = ((KeyAttribute) this.bk.getAttribute("Py04_UB_Sk_max")).getId();
    long py04UB = -1;
    long py08min = ((KeyAttribute) this.bk.getAttribute("Py08_Feuchte_min")).getId();
    long py08max = ((KeyAttribute) this.bk.getAttribute("Py08_Feuchte_max")).getId();
    long py09min = ((KeyAttribute) this.bk.getAttribute("Py09_WSV_min")).getId();
    long py09max = ((KeyAttribute) this.bk.getAttribute("Py09_WSV_max")).getId();

    // 1a) Abschätzung des relevanten Porenvolumens auf Basis der Bodenart

    // check
    if (bodenartOB1 < 0 && bodenartOB2 < 0) {
      this.resultMap.put("Leben_Tr", "FEHLER : Es muss mind. eine Bodenart für den Oberboden definiert werden");
      return;
    }
    if (bodenartUB1 < 0 && bodenartUB2 < 0) {
      this.resultMap.put("Leben_Tr", "FEHLER : Es muss mind. eine Bodenart für den Unterboden definiert werden");
      return;
    }

    // Berechnung nFK für OB und UB
    double nfkOB1 = -1;
    if (bodenartOB1 == 202) {
      nfkOB1 = 14;
    } else if (bodenartOB1 == 504) {
      nfkOB1 = 14.5;
    } else if (bodenartOB1 == 534) {
      nfkOB1 = 14.5;
    } else if (bodenartOB1 == 101) {
      nfkOB1 = 14.5;
    } else if (bodenartOB1 == 341) {
      nfkOB1 = 15;
    } else if (bodenartOB1 == 414) {
      nfkOB1 = 15.5;
    } else if (bodenartOB1 == 403) {
      nfkOB1 = 15.5;
    } else if (bodenartOB1 == 423) {
      nfkOB1 = 16;
    } else if (bodenartOB1 == 313) {
      nfkOB1 = 17;
    } else if (bodenartOB1 == 332) {
      nfkOB1 = 19;
    } else if (bodenartOB1 == 231) {
      nfkOB1 = 21;
    } else if (bodenartOB1 == 121) {
      nfkOB1 = 23;
    } else if (bodenartOB1 == 212) {
      nfkOB1 = 23.5;
    }

    double nfkOB2 = -1;
    if (bodenartOB2 == 202) {
      nfkOB2 = 14;
    } else if (bodenartOB2 == 504) {
      nfkOB2 = 14.5;
    } else if (bodenartOB2 == 534) {
      nfkOB2 = 14.5;
    } else if (bodenartOB2 == 101) {
      nfkOB2 = 14.5;
    } else if (bodenartOB2 == 341) {
      nfkOB2 = 15;
    } else if (bodenartOB2 == 414) {
      nfkOB2 = 15.5;
    } else if (bodenartOB2 == 403) {
      nfkOB2 = 15.5;
    } else if (bodenartOB2 == 423) {
      nfkOB2 = 16;
    } else if (bodenartOB2 == 313) {
      nfkOB2 = 17;
    } else if (bodenartOB2 == 332) {
      nfkOB2 = 19;
    } else if (bodenartOB2 == 231) {
      nfkOB2 = 21;
    } else if (bodenartOB2 == 121) {
      nfkOB2 = 23;
    } else if (bodenartOB2 == 212) {
      nfkOB2 = 23.5;
    }

    double nfkUB1 = -1;
    if (bodenartUB1 == 202) {
      nfkUB1 = 14;
    } else if (bodenartUB1 == 504) {
      nfkUB1 = 14.5;
    } else if (bodenartUB1 == 534) {
      nfkUB1 = 14.5;
    } else if (bodenartUB1 == 101) {
      nfkUB1 = 14.5;
    } else if (bodenartUB1 == 341) {
      nfkUB1 = 15;
    } else if (bodenartUB1 == 414) {
      nfkUB1 = 15.5;
    } else if (bodenartUB1 == 403) {
      nfkUB1 = 15.5;
    } else if (bodenartUB1 == 423) {
      nfkUB1 = 16;
    } else if (bodenartUB1 == 313) {
      nfkUB1 = 17;
    } else if (bodenartUB1 == 332) {
      nfkUB1 = 19;
    } else if (bodenartUB1 == 231) {
      nfkUB1 = 21;
    } else if (bodenartUB1 == 121) {
      nfkUB1 = 23;
    } else if (bodenartUB1 == 212) {
      nfkUB1 = 23.5;
    }

    double nfkUB2 = -1;
    if (bodenartUB2 == 202) {
      nfkUB2 = 14;
    } else if (bodenartUB2 == 504) {
      nfkUB2 = 14.5;
    } else if (bodenartUB2 == 534) {
      nfkUB2 = 14.5;
    } else if (bodenartUB2 == 101) {
      nfkUB2 = 14.5;
    } else if (bodenartUB2 == 341) {
      nfkUB2 = 15;
    } else if (bodenartUB2 == 414) {
      nfkUB2 = 15.5;
    } else if (bodenartUB2 == 403) {
      nfkUB2 = 15.5;
    } else if (bodenartUB2 == 423) {
      nfkUB2 = 16;
    } else if (bodenartUB2 == 313) {
      nfkUB2 = 17;
    } else if (bodenartUB2 == 332) {
      nfkUB2 = 19;
    } else if (bodenartUB2 == 231) {
      nfkUB2 = 21;
    } else if (bodenartUB2 == 121) {
      nfkUB2 = 23;
    } else if (bodenartUB2 == 212) {
      nfkUB2 = 23.5;
    }

    // nFK für OB und UB (Durchschnitt)
    double nfkOben = -1;
    if (nfkOB1 >= 0 && nfkOB2 >= 0) {
      nfkOben = (nfkOB1 + nfkOB2) / 2;
    } else if (nfkOB1 >= 0 && nfkOB2 < 0) {
      nfkOben = nfkOB1;
    } else if (nfkOB1 < 0 && nfkOB2 >= 0) {
      nfkOben = nfkOB2;
    }
    if (nfkOben < 0) {
      this.resultMap.put("Leben_Tr", "FEHLER : Die nFK konnte für den Obderboden nicht berechnet werden");
      return;
    }

    double nfkUnten = -1;
    if (nfkUB1 >= 0 && nfkUB2 >= 0) {
      nfkUnten = (nfkUB1 + nfkUB2) / 2;
    } else if (nfkUB1 >= 0 && nfkUB2 < 0) {
      nfkUnten = nfkUB1;
    } else if (nfkUB1 < 0 && nfkUB2 >= 0) {
      nfkUnten = nfkUB2;
    }
    if (nfkUnten < 0) {
      this.resultMap.put("Leben_Tr", "FEHLER : Die nFK konnte für den Unterboden nicht berechnet werden");
      return;
    }

    // 1b) Korrektur von nfkOben nach dem Humusgehalt (Py03)

    // Humus MIN
    double humusMin = -1;
    double nfkMin = -1;
    if (py03min == 0) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = 0;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 0;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 0;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 0;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 0;
      }
    } else if (py03min == 1) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = 0.5;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 0.5;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 0.5;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 0.5;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 1;
      }
    } else if (py03min == 2) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = 1;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 1;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 1;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 1.5;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 2.5;
      }
    } else if (py03min == 3) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = 3;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 3;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 3.5;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 4;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 5.5;
      }
    } else if (py03min == 4) {
      nfkMin = 37;
    } else if (py03min == 5) {
      nfkMin = 50;
    }
    // Humus MAX
    double humusMax = -1;
    double nfkMax = -1;
    if (py03max == 0) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = 0;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 0;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 0;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 0;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 0;
      }
    } else if (py03max == 1) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = 0.5;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 0.5;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 0.5;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 0.5;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 1;
      }
    } else if (py03max == 2) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = 1;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 1;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 1;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 1.5;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 2.5;
      }
    } else if (py03max == 3) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = 3;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 3;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 3.5;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 4;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 5.5;
      }
    } else if (py03max == 4) {
      nfkMax = 37;
    } else if (py03max == 5) {
      nfkMax = 50;
    }

    // Korrektur Humus
    if (humusMin >= 0 && humusMax >= 0) {
      // Mittelwert beider Korrekturwerte, da sie beide gesetzt wurden
      double humusCorr = (humusMin + humusMax) / 2;
      nfkOben += humusCorr;
    } else if (humusMin >= 0 && humusMax < 0) {
      // Mittelwert aus Korrektur und Ausnahme (nfkMax), falls gesetzt
      double nfkObenMod = nfkOben + humusMin;
      if (nfkMax > 0) {
        nfkOben = (nfkObenMod + nfkMax) / 2;
      } else {
        nfkOben += humusMin;
      }
    } else if (humusMin < 0 && humusMax >= 0) {
      // Mittelwert aus Korrektur und Ausnahme (nfkMin), falls gesetzt
      double nfkObenMod = nfkOben + humusMax;
      if (nfkMin > 0) {
        nfkOben = (nfkObenMod + nfkMin) / 2;
      } else {
        nfkOben += humusMax;
      }
    } else if (humusMin < 0 && humusMax < 0 && nfkMin > 0 && nfkMax > 0) {
      nfkOben = (nfkMin + nfkMax) / 2;
    }

    // 1c) Berechnung der nutzbaren Feldkapazität in [l/m²]

    if (py01 < 0) {
      this.resultMap.put("Leben_Tr", "FEHLER : Mächtigkeit des Oberbodens nicht vorhanden");
      return;
    }

    // Skelettgehalt Oberboden
    if (py04OBmin >= 0 && py04OBmax >= 0) {
      py04OB = (py04OBmin + py04OBmax) / 2;
    } else if (py04OBmin >= 0 && py04OBmax < 0) {
      py04OB = py04OBmin;
    } else if (py04OBmin < 0 && py04OBmax >= 0) {
      py04OB = py04OBmax;
    }
    if (py04OB < 0) {
      this.resultMap.put("Leben_Tr", "FEHLER : Skelettgehalt des Oberbodens nicht vorhanden");
      return;
    }

    // Skelettgehalt Unterboden
    if (py04UBmin >= 0 && py04UBmax >= 0) {
      py04UB = (py04UBmin + py04UBmax) / 2;
    } else if (py04UBmin >= 0 && py04UBmax < 0) {
      py04UB = py04UBmin;
    } else if (py04UBmin < 0 && py04UBmax >= 0) {
      py04UB = py04UBmax;
    }
    if (py04UB < 0) {
      this.resultMap.put("Leben_Tr", "FEHLER : Skelettgehalt des Unterbodens nicht vorhanden");
      return;
    }

    double nFKsum = -1;
    if (py01 >= 0 && py04OB >= 0 && nfkOben >= 0 && py02 >= 0 && py04UB >= 0 && nfkUnten >= 0) {
      nFKsum = (py01 * 10 * (1 - py04OB / 100)) * (nfkOben / 100)
              + (py02 * 10 * (1 - py04UB / 100)) * (nfkUnten / 100);
    }

    // 2. Schritt: Bewertung des Potenzials als Feuchtstandort

    long bodentypkurz = ((KeyAttribute) this.bk.getAttribute("Py07_Typ")).getId();
    if (bodentypkurz == 1) {
      result = 1;
    } else {
      // ckeck Feuchtestufe
      if (py08min < 0 && py08max < 0) {
        this.resultMap.put("Leben_Tr", "FEHLER : Es fehlt die Angabe zur Feuchtestufe");
        return;
      }
      // Feuchtestufe (wird für beide weitere Bewertungen benötigt (mit nFK und mit WSV)
      // Feuchtestufe min
      int resultPy08min = -1;
      if (py08min == 7) {
        resultPy08min = 1;
      } else if (py08min == 6) {
        resultPy08min = 2;
      } else if (py08min == 5 || py08min == 8 || py08min == 9) {
        resultPy08min = 3;
      } else if (py08min == 3 || py08min == 4) {
        resultPy08min = 4;
      } else if (py08min == 1 || py08min == 2) {
        resultPy08min = 5;
      }
      // Feuchtestufe max
      int resultPy08max = -1;
      if (py08max == 7) {
        resultPy08max = 1;
      } else if (py08max == 6) {
        resultPy08max = 2;
      } else if (py08max == 5 || py08max == 8 || py08max == 9) {
        resultPy08max = 3;
      } else if (py08max == 3 || py08max == 4) {
        resultPy08max = 4;
      } else if (py08max == 1 || py08max == 2) {
        resultPy08max = 5;
      }
      // Mittelwert Feuchtestufe
      double resultPy08 = -1;
      if (resultPy08min > 0 && resultPy08max > 0) {
        resultPy08 = (resultPy08min + resultPy08max) / 2;
      } else if (resultPy08min > 0 && resultPy08max <= 0) {
        resultPy08 = resultPy08min;
      } else if (resultPy08min <= 0 && resultPy08max > 0) {
        resultPy08 = resultPy08max;
      }
      if (resultPy08 < 0) {
        this.resultMap.put("Leben_Tr", "FEHLER: Bewertung nach der Feuchtestufe konnte nicht durchgeführt werden");
        return;
      }

      if (nFKsum >= 0) {
        // Bewertung mit nFK und Feuchtestufe

        // nFK
        int resultnFK = -1;
        if (nFKsum <= 60) {
          resultnFK = 5;
        } else if (nFKsum > 60 && nFKsum <= 140) {
          resultnFK = 4;
        } else if (nFKsum > 140 && nFKsum <= 220) {
          resultnFK = 3;
        } else if (nFKsum > 220) {
          resultnFK = 2;
        }
        if (resultnFK < 0) {
          this.resultMap.put("Leben_Tr", "FEHLER: Bewertung nach der nFK konnte nicht durchgeführt werden");
          return;
        }
        // Ergebnis
        Double bew = Math.floor((resultnFK + resultPy08) / 2);
        result = bew.intValue();

      } else {
        // Bewertung mit WSV und Feuchtestufe

        // check WSV
        if (py09min < 0 && py09max < 0) {
          this.resultMap.put("Leben_Tr", "FEHLER : Es fehlt die Angabe zur Speicherkraft");
          return;
        }
        // WSV min
        int resultPy09min = -1;
        if (py09min == 1) {
          resultPy09min = 2;
        } else if (py09min == 2) {
          resultPy09min = 3;
        } else if (py09min == 3) {
          resultPy09min = 4;
        } else if (py09min == 4 || py09min == 5) {
          resultPy09min = 5;
        }
        // WSV max
        int resultPy09max = -1;
        if (py09max == 1) {
          resultPy09max = 2;
        } else if (py09max == 2) {
          resultPy09max = 3;
        } else if (py09max == 3) {
          resultPy09max = 4;
        } else if (py09max == 4 || py09max == 5) {
          resultPy09max = 5;
        }
        // Mittelwert WSV
        double resultPy09 = -1;
        if (resultPy09min > 0 && resultPy09max > 0) {
          resultPy09 = (resultPy09min + resultPy09max) / 2;
        } else if (resultPy09min > 0 && resultPy09max <= 0) {
          resultPy09 = resultPy09min;
        } else if (resultPy09min <= 0 && resultPy09max > 0) {
          resultPy09 = resultPy09max;
        }
        if (resultPy09 < 0) {
          this.resultMap.put("Leben_Tr", "FEHLER: Bewertung nach der Feuchtestufe konnte nicht durchgeführt werden");
          return;
        }

        // Ergebnis
        Double bew = Math.floor((resultPy08 + resultPy09) / 2);
        result = bew.intValue();
      }
    }
    // Bodentyp G und PG
    if ((bodentypkurz == 2 || bodentypkurz == 3) && result != 1) {
      result = 2;
    }

    if (result != null) {
      // save to horizont
      this.bk.setAttributeValue("Leben_Fe", String.valueOf(result));
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': 1A.2 - Humid saved with value '" + result + "'");
      this.resultMap.put("Leben_Fe", String.valueOf(result));
    } else {
      this.resultMap.put("Leben_Fe", "FEHLER : Konnte nicht ermittelt werden");
    }
  }

  public void calculate1A3() throws Exception {
    Integer result = null;

    // get attributes
    long bodenartOB1 = ((KeyAttribute) this.bk.getAttribute("Bodenart_OB_1")).getId();
    long nutzung = ((KeyAttribute) this.bk.getAttribute("Landnutzung")).getId();
    long bodentypkurz = ((KeyAttribute) this.bk.getAttribute("Py07_Typ")).getId();
    long py06OBmin = ((KeyAttribute) this.bk.getAttribute("Py06_OB_pH_min")).getId();
    long py06OBmax = ((KeyAttribute) this.bk.getAttribute("Py06_OB_pH_max")).getId();
    int py06OB = -1;
    long py08min = ((KeyAttribute) this.bk.getAttribute("Py08_Feuchte_min")).getId();
    long py08max = ((KeyAttribute) this.bk.getAttribute("Py08_Feuchte_max")).getId();
    long py08 = -1;

    // calculate average values
    if (py06OBmin >= 0 && py06OBmax >= 0) {
      py06OB = Math.round((py06OBmin + py06OBmax) / 2);
    } else if (py06OBmin >= 0 && py06OBmax < 0) {
      py06OB = Long.valueOf(py06OBmin).intValue();
    } else if (py06OBmin < 0 && py06OBmax >= 0) {
      py06OB = Long.valueOf(py06OBmax).intValue();
    }

    if (py08min >= 0 && py08max >= 0) {
      py08 = Math.round((py08min + py08max) / 2);
    } else if (py08min >= 0 && py08max < 0) {
      py08 = py08min;
    } else if (py08min < 0 && py08max >= 0) {
      py08 = py08max;
    }

    // check
    if (py06OB < 0) {
      this.resultMap.put("Leben_Org", "FEHLER : pH-Wert (Bodenreaktion) konnte nicht ermittelt werden");
      return;
    } else if (py06OB == 2 || py06OB == 3 || py06OB == 4 || py06OB == 5) {
      // BLGT A
      if (py08 < 0) {
        this.resultMap.put("Leben_Org", "FEHLER : Feuchtestufe  konnte nicht ermittelt werden");
        return;
      } else if (py08 == 2 || py08 == 3 || py08 == 4 || py08 == 5 || py08 == 6 || py08 == 8 || py08 == 9) {
        if (nutzung < 0) {
          this.resultMap.put("Leben_Org", "FEHLER : Landnutzung nicht vorhanden");
          return;
        } else if (nutzung == 100) {
          // A1.1
          result = 3;
        } else if (nutzung == 200 || nutzung == 250 || nutzung == 2990) {
          if (py08 != 6 & py08 != 9) {
            // A1.2
            if (bodenartOB1 < 0) {
              this.resultMap.put("Leben_Org", "FEHLER : Bodenart des Oberbodens nicht vorhanden");
              return;
            } else if (bodenartOB1 == 101 || bodenartOB1 == 121 || bodenartOB1 == 231 || bodenartOB1 == 341) {
              // A1.2.1
              result = 2;
            } else if (bodenartOB1 == 212 || bodenartOB1 == 202 || bodenartOB1 == 332
                    || bodenartOB1 == 313 || bodenartOB1 == 403 || bodenartOB1 == 423) {
              // A1.2.2
              result = 1;
            } else if ((bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) || bodentypkurz == 1) {
              // A1.2.3
              result = 2;
            }
          } else if (py08 == 6 || py08 == 9) {
            // A1.3
            result = 3;
          }
        } else if (nutzung == 300) {
          // A1.4
          if (bodenartOB1 < 0) {
            this.resultMap.put("Leben_Org", "FEHLER : Bodenart des Oberbodens nicht vorhanden");
            return;
          } else if (bodenartOB1 == 101) {
            // A1.4.1
            result = 4;
          } else if (bodenartOB1 == 121 || bodenartOB1 == 231 || bodenartOB1 == 341
                  || bodenartOB1 == 212 || bodenartOB1 == 202 || bodenartOB1 == 332
                  || bodenartOB1 == 313 || bodenartOB1 == 403 || bodenartOB1 == 423) {
            // A1.4.2
            result = 2;
          } else if ((bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) || bodentypkurz == 1) {
            // A1.4.3
            result = 3;
          }
        }
      } else if (py08 == 7 && py06OB >= 3) {
        // A2.1
        result = 4;
      } else if (py08 == 7 && py06OB == 2) {
        // A2.2
        result = 4;
      } else if (py08 == 1) {
        // A3
        result = 4;
      } else {
        this.resultMap.put("Leben_Org", "FEHLER : Ungültiger Wert (Feuchtestufe)");
        return;
      }
    } else if (py06OB == 1) {
      // BLGT B
      if (py08 < 0) {
        this.resultMap.put("Leben_Org", "FEHLER : Feuchtestufe  konnte nicht ermittelt werden");
        return;
      } else if (py08 == 1) {
        result = 5;
      } else if (py08 == 2 || py08 == 3 || py08 == 4 || py08 == 5 || py08 == 6) {
        result = 4;
      } else if (py08 == 7) {
        result = 5;
      } else {
        this.resultMap.put("Leben_Org", "FEHLER : Ungültiger Wert (Feuchtestufe)");
        return;
      }
    } else {
      this.resultMap.put("Leben_Org", "FEHLER : Ungültiger Wert (Bodenreaktion)");
      return;
    }

    if (result != null) {
      // save to horizont
      this.bk.setAttributeValue("Leben_Org", String.valueOf(result));
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': 1A.3 saved with value '" + result + "'");
      this.resultMap.put("Leben_Org", String.valueOf(result));
    } else {
      this.resultMap.put("Leben_Org", "FEHLER : Konnte nicht ermittelt werden");
    }
  }

  public void calculate1A4() throws Exception {

    // Long needed for rounding
    Integer result = null;

    long py13 = ((KeyAttribute) this.bk.getAttribute("Py13_BWA")).getId();
    long py14 = ((KeyAttribute) this.bk.getAttribute("Py14_BWG")).getId();

    // calculate average values
    if (py13 > 0 && py14 > 0) {
      double average = (py13 + py14) / 2;
      result = Double.valueOf(Math.floor(average)).intValue();
    } else if (py13 > 0 && py14 <= 0) {
      result = Long.valueOf(py13).intValue();
    } else if (py13 <= 0 && py14 > 0) {
      result = Long.valueOf(py14).intValue();
    }

    if (result != null) {
      // save to horizont
      this.bk.setAttributeValue("Leben_Kult", String.valueOf(result));
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': 1A.4 saved with value '" + result + "'");
      this.resultMap.put("Leben_Kult", String.valueOf(result));
    } else {
      this.resultMap.put("Leben_Kult", "FEHLER : Konnte nicht ermittelt werden");
    }
  }

  public void calculate1C1() throws Exception {
    Integer result = null;

    // get attributes
    long bodenartOB1 = ((KeyAttribute) this.bk.getAttribute("Bodenart_OB_1")).getId();
    long bodenartOB2 = ((KeyAttribute) this.bk.getAttribute("Bodenart_OB_2")).getId();
    long bodenartUB1 = ((KeyAttribute) this.bk.getAttribute("Bodenart_UB_1")).getId();
    long bodenartUB2 = ((KeyAttribute) this.bk.getAttribute("Bodenart_UB_2")).getId();

    float py01 = this.bk.getAttributeValueFloat("Py01_OB_cm");
    float py02 = this.bk.getAttributeValueFloat("Py02_UB_cm");
    long py03min = ((KeyAttribute) this.bk.getAttribute("Py03_Hu_min")).getId();
    long py03max = ((KeyAttribute) this.bk.getAttribute("Py03_Hu_max")).getId();
    long py04OBmin = ((KeyAttribute) this.bk.getAttribute("Py04_OB_Sk_min")).getId();
    long py04OBmax = ((KeyAttribute) this.bk.getAttribute("Py04_OB_Sk_max")).getId();
    long py04OB = -1;
    long py04UBmin = ((KeyAttribute) this.bk.getAttribute("Py04_UB_Sk_min")).getId();
    long py04UBmax = ((KeyAttribute) this.bk.getAttribute("Py04_UB_Sk_max")).getId();
    long py04UB = -1;
    long py09min = ((KeyAttribute) this.bk.getAttribute("Py09_WSV_min")).getId();
    long py09max = ((KeyAttribute) this.bk.getAttribute("Py09_WSV_max")).getId();
    long py10min = ((KeyAttribute) this.bk.getAttribute("Py10_kf_min")).getId();
    long py10max = ((KeyAttribute) this.bk.getAttribute("Py10_kf_max")).getId();

    // 1a) Abschätzung des relevanten Porenvolumens auf Basis der Bodenart

    // check
    if (bodenartOB1 < 0 && bodenartOB2 < 0) {
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': Retention - Keine Bodenart für den Oberboden definiert");
    }
    if (bodenartUB1 < 0 && bodenartUB2 < 0) {
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': Retention - Keine Bodenart für den Unterboden definiert");
    }

    // Berechnung WSV für OB und UB
    double wsvOB1 = -1;
    if (bodenartOB1 == 504) {
      wsvOB1 = 17.5;
    } else if (bodenartOB1 == 534) {
      wsvOB1 = 17.5;
    } else if (bodenartOB1 == 202) {
      wsvOB1 = 18.5;
    } else if (bodenartOB1 == 403) {
      wsvOB1 = 20.5;
    } else if (bodenartOB1 == 423) {
      wsvOB1 = 21.5;
    } else if (bodenartOB1 == 414) {
      wsvOB1 = 23;
    } else if (bodenartOB1 == 313) {
      wsvOB1 = 23.5;
    } else if (bodenartOB1 == 332) {
      wsvOB1 = 25;
    } else if (bodenartOB1 == 341) {
      wsvOB1 = 27;
    } else if (bodenartOB1 == 231) {
      wsvOB1 = 28.5;
    } else if (bodenartOB1 == 212) {
      wsvOB1 = 29;
    } else if (bodenartOB1 == 121) {
      wsvOB1 = 30;
    } else if (bodenartOB1 == 101) {
      wsvOB1 = 30.5;
    }

    double wsvOB2 = -1;
    if (bodenartOB2 == 504) {
      wsvOB2 = 17.5;
    } else if (bodenartOB2 == 534) {
      wsvOB2 = 17.5;
    } else if (bodenartOB2 == 202) {
      wsvOB2 = 18.5;
    } else if (bodenartOB2 == 403) {
      wsvOB2 = 20.5;
    } else if (bodenartOB2 == 423) {
      wsvOB2 = 21.5;
    } else if (bodenartOB2 == 414) {
      wsvOB2 = 23;
    } else if (bodenartOB2 == 313) {
      wsvOB2 = 23.5;
    } else if (bodenartOB2 == 332) {
      wsvOB2 = 25;
    } else if (bodenartOB2 == 341) {
      wsvOB2 = 27;
    } else if (bodenartOB2 == 231) {
      wsvOB2 = 28.5;
    } else if (bodenartOB2 == 212) {
      wsvOB2 = 29;
    } else if (bodenartOB2 == 121) {
      wsvOB2 = 30;
    } else if (bodenartOB2 == 101) {
      wsvOB2 = 30.5;
    }

    double wsvUB1 = -1;
    if (bodenartUB1 == 504) {
      wsvUB1 = 17.5;
    } else if (bodenartUB1 == 534) {
      wsvUB1 = 17.5;
    } else if (bodenartUB1 == 202) {
      wsvUB1 = 18.5;
    } else if (bodenartUB1 == 403) {
      wsvUB1 = 20.5;
    } else if (bodenartUB1 == 423) {
      wsvUB1 = 21.5;
    } else if (bodenartUB1 == 414) {
      wsvUB1 = 23;
    } else if (bodenartUB1 == 313) {
      wsvUB1 = 23.5;
    } else if (bodenartUB1 == 332) {
      wsvUB1 = 25;
    } else if (bodenartUB1 == 341) {
      wsvUB1 = 27;
    } else if (bodenartUB1 == 231) {
      wsvUB1 = 28.5;
    } else if (bodenartUB1 == 212) {
      wsvUB1 = 29;
    } else if (bodenartUB1 == 121) {
      wsvUB1 = 30;
    } else if (bodenartUB1 == 101) {
      wsvUB1 = 30.5;
    }

    double wsvUB2 = -1;
    if (bodenartUB2 == 504) {
      wsvUB2 = 17.5;
    } else if (bodenartUB2 == 534) {
      wsvUB2 = 17.5;
    } else if (bodenartUB2 == 202) {
      wsvUB2 = 18.5;
    } else if (bodenartUB2 == 403) {
      wsvUB2 = 20.5;
    } else if (bodenartUB2 == 423) {
      wsvUB2 = 21.5;
    } else if (bodenartUB2 == 414) {
      wsvUB2 = 23;
    } else if (bodenartUB2 == 313) {
      wsvUB2 = 23.5;
    } else if (bodenartUB2 == 332) {
      wsvUB2 = 25;
    } else if (bodenartUB2 == 341) {
      wsvUB2 = 27;
    } else if (bodenartUB2 == 231) {
      wsvUB2 = 28.5;
    } else if (bodenartUB2 == 212) {
      wsvUB2 = 29;
    } else if (bodenartUB2 == 121) {
      wsvUB2 = 30;
    } else if (bodenartUB2 == 101) {
      wsvUB2 = 30.5;
    }

    double wsvOben = -1;
    if (wsvOB1 >= 0 && wsvOB2 >= 0) {
      wsvOben = (wsvOB1 + wsvOB2) / 2;
    } else if (wsvOB1 >= 0 && wsvOB2 < 0) {
      wsvOben = wsvOB1;
    } else if (wsvOB1 < 0 && wsvOB2 >= 0) {
      wsvOben = wsvOB2;
    }
    if (wsvOben < 0) {
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': Retention - WSV konnte für den Oberboden nicht berechnet werden");
    }

    double wsvUnten = -1;
    if (wsvUB1 >= 0 && wsvUB2 >= 0) {
      wsvUnten = (wsvUB1 + wsvUB2) / 2;
    } else if (wsvUB1 >= 0 && wsvUB2 < 0) {
      wsvUnten = wsvUB1;
    } else if (wsvUB1 < 0 && wsvUB2 >= 0) {
      wsvUnten = wsvUB2;
    }
    if (wsvUnten < 0) {
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': Retention - WSV konnte für den Unterboden nicht berechnet werden");
    }

    // 1b) Korrektur dieses Wertes nach dem Humusgehalt (Py03)
    // Humus MIN
    double humusMin = -1;
    double wsvMin = -1;
    if (py03min == 0) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = 0;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 0;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 0;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 0;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 0;
      }
    } else if (py03min == 1) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = -1;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 0.5;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 1;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 1;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 1.5;
      }
    } else if (py03min == 2) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = 0;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 2;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 2.5;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 3;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 4;
      }
    } else if (py03min == 3) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = 2;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 5;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 6;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 7;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 8;
      }
    } else if (py03min == 4) {
      wsvMin = 45;
    } else if (py03min == 5) {
      wsvMin = 70;
    }

    // Humus MAX
    double humusMax = -1;
    double wsvMax = -1;
    if (py03max == 0) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = 0;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 0;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 0;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 0;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 0;
      }
    } else if (py03max == 1) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = -1;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 0.5;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 1;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 1;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 1.5;
      }
    } else if (py03max == 2) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = 0;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 2;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 2.5;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 3;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 4;
      }
    } else if (py03max == 3) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = 2;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 5;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 6;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 7;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 8;
      }
    } else if (py03max == 4) {
      wsvMax = 37;
    } else if (py03max == 5) {
      wsvMax = 50;
    }

    // Korrektur Humus
    if (humusMin >= 0 && humusMax >= 0) {
      // Mittelwert beider Korrekturwerte, da sie beide gesetzt wurden
      double humusCorr = (humusMin + humusMax) / 2;
      wsvOben += humusCorr;
    } else if (humusMin >= 0 && humusMax < 0) {
      // Mittelwert aus Korrektur und Ausnahme (wsvMax), falls gesetzt
      double wsvObenMod = wsvOben + humusMin;
      if (wsvMax > 0) {
        wsvOben = (wsvObenMod + wsvMax) / 2;
      } else {
        wsvOben += humusMin;
      }
    } else if (humusMin < 0 && humusMax >= 0) {
      // Mittelwert aus Korrektur und Ausnahme (wsvMin), falls gesetzt
      double wsvObenMod = wsvOben + humusMax;
      if (wsvMin > 0) {
        wsvOben = (wsvObenMod + wsvMin) / 2;
      } else {
        wsvOben += humusMax;
      }
    } else if (humusMin < 0 && humusMax < 0 && wsvMin > 0 && wsvMax > 0) {
      wsvOben = (wsvMin + wsvMax) / 2;
    }

    // 1c) Berechnung des Wasserspeichervermögens in [l/m²]

    if (py01 < 0) {
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': Retention - Mächtigkeit Oberboden nicht vorhanden");
    }
    if (py02 < 0) {
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': Retention - Mächtigkeit Unterboden nicht vorhanden");
    }

    // Skelettgehalt Oberboden (Durchschnitt)
    if (py04OBmin >= 0 && py04OBmax >= 0) {
      py04OB = (py04OBmin + py04OBmax) / 2;
    } else if (py04OBmin >= 0 && py04OBmax < 0) {
      py04OB = py04OBmin;
    } else if (py04OBmin < 0 && py04OBmax >= 0) {
      py04OB = py04OBmax;
    }
    if (py04OB < 0) {
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': Retention - Skelettgehalt Oberboden nicht vorhanden");
    }

    // Skelettgehalt Unterboden (Durchschnitt)
    if (py04UBmin >= 0 && py04UBmax >= 0) {
      py04UB = (py04UBmin + py04UBmax) / 2;
    } else if (py04UBmin >= 0 && py04UBmax < 0) {
      py04UB = py04UBmin;
    } else if (py04UBmin < 0 && py04UBmax >= 0) {
      py04UB = py04UBmax;
    }
    if (py04UB < 0) {
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': Retention - Skelettgehalt Unterboden nicht vorhanden");
    }

    double wsv = -1;
    if (py01 >= 0 && py02 >= 0 && py04OB >= 0 && py04UB >= 0 && wsvOben >= 0 && wsvUnten >= 0) {
      wsv = (py01 * 10 * (1 - py04OB / 100)) * (wsvOben / 100)
              + (py02 * 10 * (1 - py04UB / 100)) * (wsvUnten / 100);
    }

    // 2. Schritt: Bestimmung der Durchlässigkeit (kf-Wert) über eine Pedotransfertabelle auf Basis elementarer Parameter

    // kf Oberboden und Unterboden
    double kfOB1 = -1;
    if (bodenartOB1 == 504) {
      kfOB1 = 5;
    } else if (bodenartOB1 == 534) {
      kfOB1 = 6;
    } else if (bodenartOB1 == 202) {
      kfOB1 = 8;
    } else if (bodenartOB1 == 212) {
      kfOB1 = 11;
    } else if (bodenartOB1 == 414) {
      kfOB1 = 11;
    } else if (bodenartOB1 == 403) {
      kfOB1 = 12;
    } else if (bodenartOB1 == 332) {
      kfOB1 = 14;
    } else if (bodenartOB1 == 313) {
      kfOB1 = 16;
    } else if (bodenartOB1 == 423) {
      kfOB1 = 19;
    } else if (bodenartOB1 == 121) {
      kfOB1 = 20;
    } else if (bodenartOB1 == 231) {
      kfOB1 = 20;
    } else if (bodenartOB1 == 341) {
      kfOB1 = 48;
    } else if (bodenartOB1 == 101) {
      kfOB1 = 117;
    }

    double kfOB2 = -1;
    if (bodenartOB2 == 504) {
      kfOB2 = 5;
    } else if (bodenartOB2 == 534) {
      kfOB2 = 6;
    } else if (bodenartOB2 == 202) {
      kfOB2 = 8;
    } else if (bodenartOB2 == 212) {
      kfOB2 = 11;
    } else if (bodenartOB2 == 414) {
      kfOB2 = 11;
    } else if (bodenartOB2 == 403) {
      kfOB2 = 12;
    } else if (bodenartOB2 == 332) {
      kfOB2 = 14;
    } else if (bodenartOB2 == 313) {
      kfOB2 = 16;
    } else if (bodenartOB2 == 423) {
      kfOB2 = 19;
    } else if (bodenartOB2 == 121) {
      kfOB2 = 20;
    } else if (bodenartOB2 == 231) {
      kfOB2 = 20;
    } else if (bodenartOB2 == 341) {
      kfOB2 = 48;
    } else if (bodenartOB2 == 101) {
      kfOB2 = 117;
    }

    double kfUB1 = -1;
    if (bodenartUB1 == 504) {
      kfUB1 = 5;
    } else if (bodenartUB1 == 534) {
      kfUB1 = 6;
    } else if (bodenartUB1 == 202) {
      kfUB1 = 8;
    } else if (bodenartUB1 == 212) {
      kfUB1 = 11;
    } else if (bodenartUB1 == 414) {
      kfUB1 = 11;
    } else if (bodenartUB1 == 403) {
      kfUB1 = 12;
    } else if (bodenartUB1 == 332) {
      kfUB1 = 14;
    } else if (bodenartUB1 == 313) {
      kfUB1 = 16;
    } else if (bodenartUB1 == 423) {
      kfUB1 = 19;
    } else if (bodenartUB1 == 121) {
      kfUB1 = 20;
    } else if (bodenartUB1 == 231) {
      kfUB1 = 20;
    } else if (bodenartUB1 == 341) {
      kfUB1 = 48;
    } else if (bodenartUB1 == 101) {
      kfUB1 = 117;
    }

    double kfUB2 = -1;
    if (bodenartUB2 == 504) {
      kfUB2 = 17.5;
    } else if (bodenartUB2 == 534) {
      kfUB2 = 17.5;
    } else if (bodenartUB2 == 202) {
      kfUB2 = 18.5;
    } else if (bodenartUB2 == 403) {
      kfUB2 = 20.5;
    } else if (bodenartUB2 == 423) {
      kfUB2 = 21.5;
    } else if (bodenartUB2 == 414) {
      kfUB2 = 23;
    } else if (bodenartUB2 == 313) {
      kfUB2 = 23.5;
    } else if (bodenartUB2 == 332) {
      kfUB2 = 25;
    } else if (bodenartUB2 == 341) {
      kfUB2 = 27;
    } else if (bodenartUB2 == 231) {
      kfUB2 = 28.5;
    } else if (bodenartUB2 == 212) {
      kfUB2 = 29;
    } else if (bodenartUB2 == 121) {
      kfUB2 = 30;
    } else if (bodenartUB2 == 101) {
      kfUB2 = 30.5;
    }

    double kfOben = -1;
    if (kfOB1 >= 0 && kfOB2 >= 0) {
      kfOben = (kfOB1 + kfOB2) / 2;
    } else if (kfOB1 >= 0 && kfOB2 < 0) {
      kfOben = kfOB1;
    } else if (kfOB1 < 0 && kfOB2 >= 0) {
      kfOben = kfOB2;
    }

    double kfUnten = -1;
    if (kfUB1 >= 0 && kfUB2 >= 0) {
      kfUnten = (kfUB1 + kfUB2) / 2;
    } else if (kfUB1 >= 0 && kfUB2 < 0) {
      kfUnten = kfUB1;
    } else if (kfUB1 < 0 && kfUB2 >= 0) {
      kfUnten = kfUB2;
    }

    // Oberboden
    if (py03max == 5) {
      kfOben = 25;
    }

    if ((py04OBmin == 60 || py04OBmin == 90) && py04OBmax == 90) {
      kfOben = 300;
    } else if (py04OBmin <= 100 && py04OBmax == 100) {
      // siehe Dokumentation (keine vernünftige Aussage möglich)
      this.resultMap.put("Retention", "FEHLER : Aufgrund des Skelettgehaltes des OB ist keine vernünftige Aussage zur Durchlässigkeit möglich");
      return;
    } else if (py04OBmin == 100 && py04OBmax == 100) {
      kfOben = 1;
    }

    // Unterboden
    if ((py04UBmin == 60 || py04UBmin == 90) && py04UBmax == 90) {
      kfUnten = 300;
    } else if (py04UBmin <= 100 && py04UBmax == 100) {
      // siehe Dokumentation (keine vernünftige Aussage möglich)
      this.resultMap.put("Retention", "FEHLER : Aufgrund des Skelettgehaltes des UB ist keine vernünftige Aussage zur Durchlässigkeit möglich");
      return;
    } else if (py04UBmin <= 100 && py04UBmax == 100) {
      kfUnten = 1;
    }

    // Den kleinsten kf-Wert für die weitere Bewertung verwenden
    double kf = -1;
    if (kfUnten >= 0 && kfOben >= 0) {
      if (kfUnten < kfOben) {
        kf = kfUnten;
      } else if (kfOben < kfUnten) {
        kf = kfOben;
      }
    }

    // 3. Schritt: Gesamtbewertung Retention (vgl. DS I)

    // check
    if (kf < 0 && (py10min < 0 && py10max < 0)) {
      this.resultMap.put("Retention", "FEHLER : kf-Wert konnte nicht berechnet werden und Durchlässigkeitsangabe fehlt. Bewertung nicht möglich");
      return;
    }
    if (wsv < 0 && (py09min < 0 && py09max < 0)) {
      this.resultMap.put("Retention", "FEHLER : WSV-Wert konnte nicht berechnet werden und WSV-Klasse fehlt. Bewertung nicht möglich");
      return;
    }

    if (kf >= 0) {
      // kf verwenden
      // wsv prüfen
      if (wsv >= 0) {
        // wsv verwenden
        if (kf <= 7) {
          if (wsv <= 60) {
            result = 5;
          } else if (wsv > 60 && wsv <= 90) {
            result = 5;
          } else if (wsv > 90 && wsv <= 220) {
            result = 5;
          } else if (wsv > 220 && wsv <= 300) {
            result = 4;
          } else if (wsv > 300) {
            result = 4;
          }
        } else if (kf > 7 && kf <= 15) {
          if (wsv <= 60) {
            result = 5;
          } else if (wsv > 60 && wsv <= 90) {
            result = 5;
          } else if (wsv > 90 && wsv <= 220) {
            result = 4;
          } else if (wsv > 220 && wsv <= 300) {
            result = 3;
          } else if (wsv > 300) {
            result = 3;
          }
        } else if (kf > 15 && kf <= 40) {
          if (wsv <= 60) {
            result = 5;
          } else if (wsv > 60 && wsv <= 90) {
            result = 4;
          } else if (wsv > 90 && wsv <= 220) {
            result = 3;
          } else if (wsv > 220 && wsv <= 300) {
            result = 2;
          } else if (wsv > 300) {
            result = 2;
          }
        } else if (kf > 40 && kf <= 100) {
          if (wsv <= 60) {
            result = 3;
          } else if (wsv > 60 && wsv <= 90) {
            result = 3;
          } else if (wsv > 90 && wsv <= 220) {
            result = 2;
          } else if (wsv > 220 && wsv <= 300) {
            result = 2;
          } else if (wsv > 300) {
            result = 1;
          }
        } else if (kf > 100) {
          if (wsv <= 60) {
            result = 1;
          } else if (wsv > 60 && wsv <= 90) {
            result = 1;
          } else if (wsv > 90 && wsv <= 220) {
            result = 1;
          } else if (wsv > 220 && wsv <= 300) {
            result = 1;
          } else if (wsv > 300) {
            result = 1;
          }
        }
      } else {
        // py09 verwenden
        int resultPy09min = -1;
        int resultPy09max = -1;
        if (kf <= 7) {
          // result with py09min
          if (py09min == 5) {
            resultPy09min = 5;
          } else if (py09min == 4) {
            resultPy09min = 5;
          } else if (py09min == 3) {
            resultPy09min = 5;
          } else if (py09min == 2) {
            resultPy09min = 4;
          } else if (py09min == 1) {
            resultPy09min = 4;
          }
          // result with py09max
          if (py09max == 5) {
            resultPy09max = 5;
          } else if (py09max == 4) {
            resultPy09max = 5;
          } else if (py09max == 3) {
            resultPy09max = 5;
          } else if (py09max == 2) {
            resultPy09max = 4;
          } else if (py09max == 1) {
            resultPy09max = 4;
          }
        } else if (kf > 7 && kf <= 15) {
          // result with py09min
          if (py09min == 5) {
            resultPy09min = 5;
          } else if (py09min == 4) {
            resultPy09min = 5;
          } else if (py09min == 3) {
            resultPy09min = 4;
          } else if (py09min == 2) {
            resultPy09min = 3;
          } else if (py09min == 1) {
            resultPy09min = 3;
          }
          // result with py09max
          if (py09max == 5) {
            resultPy09max = 5;
          } else if (py09max == 4) {
            resultPy09max = 5;
          } else if (py09max == 3) {
            resultPy09max = 4;
          } else if (py09max == 2) {
            resultPy09max = 3;
          } else if (py09max == 1) {
            resultPy09max = 3;
          }
        } else if (kf > 15 && kf <= 40) {
          // result with py09min
          if (py09min == 5) {
            resultPy09min = 5;
          } else if (py09min == 4) {
            resultPy09min = 4;
          } else if (py09min == 3) {
            resultPy09min = 3;
          } else if (py09min == 2) {
            resultPy09min = 2;
          } else if (py09min == 1) {
            resultPy09min = 2;
          }
          // result with py09max
          if (py09max == 5) {
            resultPy09max = 5;
          } else if (py09max == 4) {
            resultPy09max = 4;
          } else if (py09max == 3) {
            resultPy09max = 3;
          } else if (py09max == 2) {
            resultPy09max = 2;
          } else if (py09max == 1) {
            resultPy09max = 2;
          }
        } else if (kf > 40 && kf <= 100) {
          // result with py09min
          if (py09min == 5) {
            resultPy09min = 3;
          } else if (py09min == 4) {
            resultPy09min = 3;
          } else if (py09min == 3) {
            resultPy09min = 2;
          } else if (py09min == 2) {
            resultPy09min = 2;
          } else if (py09min == 1) {
            resultPy09min = 1;
          }
          // result with py09max
          if (py09max == 5) {
            resultPy09max = 3;
          } else if (py09max == 4) {
            resultPy09max = 3;
          } else if (py09max == 3) {
            resultPy09max = 2;
          } else if (py09max == 2) {
            resultPy09max = 2;
          } else if (py09max == 1) {
            resultPy09max = 1;
          }
        } else if (kf > 100) {
          // result with py09min
          if (py09min == 5) {
            resultPy09min = 1;
          } else if (py09min == 4) {
            resultPy09min = 1;
          } else if (py09min == 3) {
            resultPy09min = 1;
          } else if (py09min == 2) {
            resultPy09min = 1;
          } else if (py09min == 1) {
            resultPy09min = 1;
          }
          // result with py09max
          if (py09max == 5) {
            resultPy09max = 1;
          } else if (py09max == 4) {
            resultPy09max = 1;
          } else if (py09max == 3) {
            resultPy09max = 1;
          } else if (py09max == 2) {
            resultPy09max = 1;
          } else if (py09max == 1) {
            resultPy09max = 1;
          }
        }
        // Durchschnitt von resultPy09min und resultPy09max
        if (resultPy09min > 0 && resultPy09max > 0) {
          result = Double.valueOf(Math.floor((resultPy09min + resultPy09max) / 2)).intValue();
        } else if (resultPy09min > 0 && resultPy09max < 0) {
          result = resultPy09min;
        } else if (resultPy09min < 0 && resultPy09max > 0) {
          result = resultPy09max;
        }
      }
    } else if (py10min >= 0 && py10max >= 0) {
      // Durchlässigkeit verwenden (py10)

      if (wsv >= 0) {
        // wsv verwenden
        int resultPy10min = -1;
        int resultPy10max = -1;
        if (wsv <= 60) {
          // result with py10min
          if (py10min == 5) {
            resultPy10min = 5;
          } else if (py10min == 4) {
            resultPy10min = 5;
          } else if (py10min == 3) {
            resultPy10min = 5;
          } else if (py10min == 2) {
            resultPy10min = 3;
          } else if (py10min == 1) {
            resultPy10min = 1;
          }
          // result with py10max
          if (py10max == 5) {
            resultPy10max = 5;
          } else if (py10max == 4) {
            resultPy10max = 5;
          } else if (py10max == 3) {
            resultPy10max = 5;
          } else if (py10max == 2) {
            resultPy10max = 3;
          } else if (py10max == 1) {
            resultPy10max = 1;
          }
        } else if (wsv > 60 && wsv <= 90) {
          // result with py10min
          if (py10min == 5) {
            resultPy10min = 5;
          } else if (py10min == 4) {
            resultPy10min = 5;
          } else if (py10min == 3) {
            resultPy10min = 4;
          } else if (py10min == 2) {
            resultPy10min = 3;
          } else if (py10min == 1) {
            resultPy10min = 1;
          }
          // result with py10max
          if (py10max == 5) {
            resultPy10max = 5;
          } else if (py10max == 4) {
            resultPy10max = 5;
          } else if (py10max == 3) {
            resultPy10max = 4;
          } else if (py10max == 2) {
            resultPy10max = 3;
          } else if (py10max == 1) {
            resultPy10max = 1;
          }
        } else if (wsv > 90 && wsv <= 220) {
          // result with py10min
          if (py10min == 5) {
            resultPy10min = 5;
          } else if (py10min == 4) {
            resultPy10min = 4;
          } else if (py10min == 3) {
            resultPy10min = 3;
          } else if (py10min == 2) {
            resultPy10min = 2;
          } else if (py10min == 1) {
            resultPy10min = 1;
          }
          // result with py10max
          if (py10max == 5) {
            resultPy10max = 5;
          } else if (py10max == 4) {
            resultPy10max = 4;
          } else if (py10max == 3) {
            resultPy10max = 3;
          } else if (py10max == 2) {
            resultPy10max = 2;
          } else if (py10max == 1) {
            resultPy10max = 1;
          }
        } else if (wsv > 220 && wsv <= 300) {
          // result with py10min
          if (py10min == 5) {
            resultPy10min = 4;
          } else if (py10min == 4) {
            resultPy10min = 3;
          } else if (py10min == 3) {
            resultPy10min = 2;
          } else if (py10min == 2) {
            resultPy10min = 2;
          } else if (py10min == 1) {
            resultPy10min = 1;
          }
          // result with py10max
          if (py10max == 5) {
            resultPy10max = 4;
          } else if (py10max == 4) {
            resultPy10max = 3;
          } else if (py10max == 3) {
            resultPy10max = 2;
          } else if (py10max == 2) {
            resultPy10max = 2;
          } else if (py10max == 1) {
            resultPy10max = 1;
          }
        } else if (wsv > 300) {
          // result with py10min
          if (py10min == 5) {
            resultPy10min = 4;
          } else if (py10min == 4) {
            resultPy10min = 3;
          } else if (py10min == 3) {
            resultPy10min = 2;
          } else if (py10min == 2) {
            resultPy10min = 1;
          } else if (py10min == 1) {
            resultPy10min = 1;
          }
          // result with py10max
          if (py10max == 5) {
            resultPy10max = 4;
          } else if (py10max == 4) {
            resultPy10max = 3;
          } else if (py10max == 3) {
            resultPy10max = 2;
          } else if (py10max == 2) {
            resultPy10max = 1;
          } else if (py10max == 1) {
            resultPy10max = 1;
          }
        }
        // Durchschnitt von resultPy10min und resultPy10max
        if (resultPy10min > 0 && resultPy10max > 0) {
          result = Double.valueOf(Math.floor((resultPy10min + resultPy10max) / 2)).intValue();
        } else if (resultPy10min > 0 && resultPy10max < 0) {
          result = resultPy10min;
        } else if (resultPy10min < 0 && resultPy10max > 0) {
          result = resultPy10max;
        }
      } else {
        // hier wird's kompliziert, da wir 4 Werte haben, jeweils ein max und ein min Wert (Py10, Py09)
        // int resultPY10PY09 -->
        int resultMinMin = -1; // Py10 Min und Py09 Min
        int resultMinMax = -1; // Py10 Min und Py09 Max
        int resultMaxMin = -1; // Py10 Max und Py09 Min
        int resultMaxMax = -1; // Py10 Max und Py09 Max

        // PY10 MIN
        if (py10min == 5) {
          // result with py09min
          if (py09min == 5) {
            resultMinMin = 5;
          } else if (py09min == 4) {
            resultMinMin = 5;
          } else if (py09min == 3) {
            resultMinMin = 5;
          } else if (py09min == 2) {
            resultMinMin = 4;
          } else if (py09min == 1) {
            resultMinMin = 4;
          }
          // result with py09max
          if (py09max == 5) {
            resultMinMax = 5;
          } else if (py09max == 4) {
            resultMinMax = 5;
          } else if (py09max == 3) {
            resultMinMax = 5;
          } else if (py09max == 2) {
            resultMinMax = 4;
          } else if (py09max == 1) {
            resultMinMax = 4;
          }
        } else if (py10min == 4) {
          // result with py09min
          if (py09min == 5) {
            resultMinMin = 5;
          } else if (py09min == 4) {
            resultMinMin = 5;
          } else if (py09min == 3) {
            resultMinMin = 4;
          } else if (py09min == 2) {
            resultMinMin = 3;
          } else if (py09min == 1) {
            resultMinMin = 3;
          }
          // result with py09max
          if (py09max == 5) {
            resultMinMax = 5;
          } else if (py09max == 4) {
            resultMinMax = 5;
          } else if (py09max == 3) {
            resultMinMax = 4;
          } else if (py09max == 2) {
            resultMinMax = 3;
          } else if (py09max == 1) {
            resultMinMax = 3;
          }
        } else if (py10min == 3) {
          // result with py09min
          if (py09min == 5) {
            resultMinMin = 5;
          } else if (py09min == 4) {
            resultMinMin = 4;
          } else if (py09min == 3) {
            resultMinMin = 3;
          } else if (py09min == 2) {
            resultMinMin = 2;
          } else if (py09min == 1) {
            resultMinMin = 2;
          }
          // result with py09max
          if (py09max == 5) {
            resultMinMax = 5;
          } else if (py09max == 4) {
            resultMinMax = 4;
          } else if (py09max == 3) {
            resultMinMax = 3;
          } else if (py09max == 2) {
            resultMinMax = 2;
          } else if (py09max == 1) {
            resultMinMax = 2;
          }
        } else if (py10min == 2) {
          // result with py09min
          if (py09min == 5) {
            resultMinMin = 3;
          } else if (py09min == 4) {
            resultMinMin = 3;
          } else if (py09min == 3) {
            resultMinMin = 2;
          } else if (py09min == 2) {
            resultMinMin = 2;
          } else if (py09min == 1) {
            resultMinMin = 1;
          }
          // result with py09max
          if (py09max == 5) {
            resultMinMax = 3;
          } else if (py09max == 4) {
            resultMinMax = 3;
          } else if (py09max == 3) {
            resultMinMax = 2;
          } else if (py09max == 2) {
            resultMinMax = 2;
          } else if (py09max == 1) {
            resultMinMax = 1;
          }
        } else if (py10min == 1) {
          // result with py09min
          if (py09min == 5) {
            resultMinMin = 1;
          } else if (py09min == 4) {
            resultMinMin = 1;
          } else if (py09min == 3) {
            resultMinMin = 1;
          } else if (py09min == 2) {
            resultMinMin = 1;
          } else if (py09min == 1) {
            resultMinMin = 1;
          }
          // result with py09max
          if (py09max == 5) {
            resultMinMax = 1;
          } else if (py09max == 4) {
            resultMinMax = 1;
          } else if (py09max == 3) {
            resultMinMax = 1;
          } else if (py09max == 2) {
            resultMinMax = 1;
          } else if (py09max == 1) {
            resultMinMax = 1;
          }
        }

        // PY10 MAX
        if (py10max == 5) {
          // result with py09min
          if (py09min == 5) {
            resultMaxMin = 5;
          } else if (py09min == 4) {
            resultMaxMin = 5;
          } else if (py09min == 3) {
            resultMaxMin = 5;
          } else if (py09min == 2) {
            resultMaxMin = 4;
          } else if (py09min == 1) {
            resultMaxMin = 4;
          }
          // result with py09max
          if (py09max == 5) {
            resultMaxMax = 5;
          } else if (py09max == 4) {
            resultMaxMax = 5;
          } else if (py09max == 3) {
            resultMaxMax = 5;
          } else if (py09max == 2) {
            resultMaxMax = 4;
          } else if (py09max == 1) {
            resultMaxMax = 4;
          }
        } else if (py10max == 4) {
          // result with py09min
          if (py09min == 5) {
            resultMaxMin = 5;
          } else if (py09min == 4) {
            resultMaxMin = 5;
          } else if (py09min == 3) {
            resultMaxMin = 4;
          } else if (py09min == 2) {
            resultMaxMin = 3;
          } else if (py09min == 1) {
            resultMaxMin = 3;
          }
          // result with py09max
          if (py09max == 5) {
            resultMaxMax = 5;
          } else if (py09max == 4) {
            resultMaxMax = 5;
          } else if (py09max == 3) {
            resultMaxMax = 4;
          } else if (py09max == 2) {
            resultMaxMax = 3;
          } else if (py09max == 1) {
            resultMaxMax = 3;
          }
        } else if (py10max == 3) {
          // result with py09min
          if (py09min == 5) {
            resultMaxMin = 5;
          } else if (py09min == 4) {
            resultMaxMin = 4;
          } else if (py09min == 3) {
            resultMaxMin = 3;
          } else if (py09min == 2) {
            resultMaxMin = 2;
          } else if (py09min == 1) {
            resultMaxMin = 2;
          }
          // result with py09max
          if (py09max == 5) {
            resultMaxMax = 5;
          } else if (py09max == 4) {
            resultMaxMax = 4;
          } else if (py09max == 3) {
            resultMaxMax = 3;
          } else if (py09max == 2) {
            resultMaxMax = 2;
          } else if (py09max == 1) {
            resultMaxMax = 2;
          }
        } else if (py10max == 2) {
          // result with py09min
          if (py09min == 5) {
            resultMaxMin = 3;
          } else if (py09min == 4) {
            resultMaxMin = 3;
          } else if (py09min == 3) {
            resultMaxMin = 2;
          } else if (py09min == 2) {
            resultMaxMin = 2;
          } else if (py09min == 1) {
            resultMaxMin = 1;
          }
          // result with py09max
          if (py09max == 5) {
            resultMaxMax = 3;
          } else if (py09max == 4) {
            resultMaxMax = 3;
          } else if (py09max == 3) {
            resultMaxMax = 2;
          } else if (py09max == 2) {
            resultMaxMax = 2;
          } else if (py09max == 1) {
            resultMaxMax = 1;
          }
        } else if (py10max == 1) {
          // result with py09min
          if (py09min == 5) {
            resultMaxMin = 1;
          } else if (py09min == 4) {
            resultMaxMin = 1;
          } else if (py09min == 3) {
            resultMaxMin = 1;
          } else if (py09min == 2) {
            resultMaxMin = 1;
          } else if (py09min == 1) {
            resultMaxMin = 1;
          }
          // result with py09max
          if (py09max == 5) {
            resultMaxMax = 1;
          } else if (py09max == 4) {
            resultMaxMax = 1;
          } else if (py09max == 3) {
            resultMaxMax = 1;
          } else if (py09max == 2) {
            resultMaxMax = 1;
          } else if (py09max == 1) {
            resultMaxMax = 1;
          }
        }

        // Durchschnitt aller vier Werte berechnen
        int[] resultArr = new int[]{resultMinMin, resultMinMax, resultMaxMin, resultMaxMax};

        int count = 0;
        int sum = 0;
        for (Integer val : resultArr) {
          if (val > 0) {
            sum += val;
            ++count;
          }
        }
        if (sum > 0) {
          double durchschnitt = sum / count;
          result = Double.valueOf(Math.floor(durchschnitt)).intValue();
        }
      }
    }

    if (result != null) {
      // save to horizont
      this.bk.setAttributeValue("Retention", String.valueOf(result));
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': 1C.1 average saved with value '" + result + "'");
      this.resultMap.put("Retention", String.valueOf(result));
    } else {
      this.resultMap.put("Retention", "FEHLER : Konnte nicht ermittelt werden");
    }
  }

  public void calculate1C3() throws Exception {
    Integer result = null;

    // get attributes
    long bodenartOB1 = ((KeyAttribute) this.bk.getAttribute("Bodenart_OB_1")).getId();
    long bodenartOB2 = ((KeyAttribute) this.bk.getAttribute("Bodenart_OB_2")).getId();
    long bodenartUB1 = ((KeyAttribute) this.bk.getAttribute("Bodenart_UB_1")).getId();
    long bodenartUB2 = ((KeyAttribute) this.bk.getAttribute("Bodenart_UB_2")).getId();

    float py01 = this.bk.getAttributeValueFloat("Py01_OB_cm");
    float py02 = this.bk.getAttributeValueFloat("Py02_UB_cm");
    long py03min = ((KeyAttribute) this.bk.getAttribute("Py03_Hu_min")).getId();
    long py03max = ((KeyAttribute) this.bk.getAttribute("Py03_Hu_max")).getId();
    long py04OBmin = ((KeyAttribute) this.bk.getAttribute("Py04_OB_Sk_min")).getId();
    long py04OBmax = ((KeyAttribute) this.bk.getAttribute("Py04_OB_Sk_max")).getId();
    long py04OB = -1;
    long py04UBmin = ((KeyAttribute) this.bk.getAttribute("Py04_UB_Sk_min")).getId();
    long py04UBmax = ((KeyAttribute) this.bk.getAttribute("Py04_UB_Sk_max")).getId();
    long py04UB = -1;
    long py09min = ((KeyAttribute) this.bk.getAttribute("Py09_WSV_min")).getId();
    long py09max = ((KeyAttribute) this.bk.getAttribute("Py09_WSV_max")).getId();
    long py10min = ((KeyAttribute) this.bk.getAttribute("Py10_kf_min")).getId();
    long py10max = ((KeyAttribute) this.bk.getAttribute("Py10_kf_max")).getId();

    // 1a) Abschätzung des relevanten Porenvolumens auf Basis der Bodenart

    // check
    if (bodenartOB1 < 0 && bodenartOB2 < 0) {
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': GWneu - Bodenart Oberboden nicht vorhanden");
    }
    if (bodenartUB1 < 0 && bodenartUB2 < 0) {
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': GWneu - Bodenart Unterboden nicht vorhanden");
    }

    // Berechnung WSV für OB und UB
    double wsvOB1 = -1;
    if (bodenartOB1 == 504) {
      wsvOB1 = 17.5;
    } else if (bodenartOB1 == 534) {
      wsvOB1 = 17.5;
    } else if (bodenartOB1 == 202) {
      wsvOB1 = 18.5;
    } else if (bodenartOB1 == 403) {
      wsvOB1 = 20.5;
    } else if (bodenartOB1 == 423) {
      wsvOB1 = 21.5;
    } else if (bodenartOB1 == 414) {
      wsvOB1 = 23;
    } else if (bodenartOB1 == 313) {
      wsvOB1 = 23.5;
    } else if (bodenartOB1 == 332) {
      wsvOB1 = 25;
    } else if (bodenartOB1 == 341) {
      wsvOB1 = 27;
    } else if (bodenartOB1 == 231) {
      wsvOB1 = 28.5;
    } else if (bodenartOB1 == 212) {
      wsvOB1 = 29;
    } else if (bodenartOB1 == 121) {
      wsvOB1 = 30;
    } else if (bodenartOB1 == 101) {
      wsvOB1 = 30.5;
    }

    double wsvOB2 = -1;
    if (bodenartOB2 == 504) {
      wsvOB2 = 17.5;
    } else if (bodenartOB2 == 534) {
      wsvOB2 = 17.5;
    } else if (bodenartOB2 == 202) {
      wsvOB2 = 18.5;
    } else if (bodenartOB2 == 403) {
      wsvOB2 = 20.5;
    } else if (bodenartOB2 == 423) {
      wsvOB2 = 21.5;
    } else if (bodenartOB2 == 414) {
      wsvOB2 = 23;
    } else if (bodenartOB2 == 313) {
      wsvOB2 = 23.5;
    } else if (bodenartOB2 == 332) {
      wsvOB2 = 25;
    } else if (bodenartOB2 == 341) {
      wsvOB2 = 27;
    } else if (bodenartOB2 == 231) {
      wsvOB2 = 28.5;
    } else if (bodenartOB2 == 212) {
      wsvOB2 = 29;
    } else if (bodenartOB2 == 121) {
      wsvOB2 = 30;
    } else if (bodenartOB2 == 101) {
      wsvOB2 = 30.5;
    }

    double wsvUB1 = -1;
    if (bodenartUB1 == 504) {
      wsvUB1 = 17.5;
    } else if (bodenartUB1 == 534) {
      wsvUB1 = 17.5;
    } else if (bodenartUB1 == 202) {
      wsvUB1 = 18.5;
    } else if (bodenartUB1 == 403) {
      wsvUB1 = 20.5;
    } else if (bodenartUB1 == 423) {
      wsvUB1 = 21.5;
    } else if (bodenartUB1 == 414) {
      wsvUB1 = 23;
    } else if (bodenartUB1 == 313) {
      wsvUB1 = 23.5;
    } else if (bodenartUB1 == 332) {
      wsvUB1 = 25;
    } else if (bodenartUB1 == 341) {
      wsvUB1 = 27;
    } else if (bodenartUB1 == 231) {
      wsvUB1 = 28.5;
    } else if (bodenartUB1 == 212) {
      wsvUB1 = 29;
    } else if (bodenartUB1 == 121) {
      wsvUB1 = 30;
    } else if (bodenartUB1 == 101) {
      wsvUB1 = 30.5;
    }

    double wsvUB2 = -1;
    if (bodenartUB2 == 504) {
      wsvUB2 = 17.5;
    } else if (bodenartUB2 == 534) {
      wsvUB2 = 17.5;
    } else if (bodenartUB2 == 202) {
      wsvUB2 = 18.5;
    } else if (bodenartUB2 == 403) {
      wsvUB2 = 20.5;
    } else if (bodenartUB2 == 423) {
      wsvUB2 = 21.5;
    } else if (bodenartUB2 == 414) {
      wsvUB2 = 23;
    } else if (bodenartUB2 == 313) {
      wsvUB2 = 23.5;
    } else if (bodenartUB2 == 332) {
      wsvUB2 = 25;
    } else if (bodenartUB2 == 341) {
      wsvUB2 = 27;
    } else if (bodenartUB2 == 231) {
      wsvUB2 = 28.5;
    } else if (bodenartUB2 == 212) {
      wsvUB2 = 29;
    } else if (bodenartUB2 == 121) {
      wsvUB2 = 30;
    } else if (bodenartUB2 == 101) {
      wsvUB2 = 30.5;
    }

    double wsvOben = -1;
    if (wsvOB1 >= 0 && wsvOB2 >= 0) {
      wsvOben = (wsvOB1 + wsvOB2) / 2;
    } else if (wsvOB1 >= 0 && wsvOB2 < 0) {
      wsvOben = wsvOB1;
    } else if (wsvOB1 < 0 && wsvOB2 >= 0) {
      wsvOben = wsvOB2;
    }
    if (wsvOben < 0) {
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': GWneu - WSV für den Oberboden konnte nicht berechnet werden");
    }

    double wsvUnten = -1;
    if (wsvUB1 >= 0 && wsvUB2 >= 0) {
      wsvUnten = (wsvUB1 + wsvUB2) / 2;
    } else if (wsvUB1 >= 0 && wsvUB2 < 0) {
      wsvUnten = wsvUB1;
    } else if (wsvUB1 < 0 && wsvUB2 >= 0) {
      wsvUnten = wsvUB2;
    }
    if (wsvUnten < 0) {
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': GWneu - WSV für den Unterboden konnte nicht berechnet werden");
    }

    // 1b) Korrektur dieses Wertes nach dem Humusgehalt (Py03)
    // Humus MIN
    double humusMin = -1;
    double wsvMin = -1;
    if (py03min == 0) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = 0;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 0;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 0;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 0;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 0;
      }
    } else if (py03min == 1) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = -1;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 0.5;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 1;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 1;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 1.5;
      }
    } else if (py03min == 2) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = 0;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 2;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 2.5;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 3;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 4;
      }
    } else if (py03min == 3) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = 2;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 5;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 6;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 7;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 8;
      }
    } else if (py03min == 4) {
      wsvMin = 45;
    } else if (py03min == 5) {
      wsvMin = 70;
    }

    // Humus MAX
    double humusMax = -1;
    double wsvMax = -1;
    if (py03max == 0) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = 0;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 0;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 0;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 0;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 0;
      }
    } else if (py03max == 1) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = -1;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 0.5;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 1;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 1;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 1.5;
      }
    } else if (py03max == 2) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = 0;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 2;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 2.5;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 3;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 4;
      }
    } else if (py03max == 3) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = 2;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 5;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 6;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 7;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 8;
      }
    } else if (py03max == 4) {
      wsvMax = 37;
    } else if (py03max == 5) {
      wsvMax = 50;
    }

    // Korrektur Humus
    if (humusMin >= 0 && humusMax >= 0) {
      // Mittelwert beider Korrekturwerte, da sie beide gesetzt wurden
      double humusCorr = (humusMin + humusMax) / 2;
      wsvOben += humusCorr;
    } else if (humusMin >= 0 && humusMax < 0) {
      // Mittelwert aus Korrektur und Ausnahme (wsvMax), falls gesetzt
      double wsvObenMod = wsvOben + humusMin;
      if (wsvMax > 0) {
        wsvOben = (wsvObenMod + wsvMax) / 2;
      } else {
        wsvOben += humusMin;
      }
    } else if (humusMin < 0 && humusMax >= 0) {
      // Mittelwert aus Korrektur und Ausnahme (wsvMin), falls gesetzt
      double wsvObenMod = wsvOben + humusMax;
      if (wsvMin > 0) {
        wsvOben = (wsvObenMod + wsvMin) / 2;
      } else {
        wsvOben += humusMax;
      }
    } else if (humusMin < 0 && humusMax < 0 && wsvMin > 0 && wsvMax > 0) {
      wsvOben = (wsvMin + wsvMax) / 2;
    }

    // 1c) Berechnung des Wasserspeichervermögens in [l/m²]

    if (py01 < 0) {
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': GWneu - Mächtikeit Oberboden nicht vorhanden");
    }
    if (py02 < 0) {
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': GWneu - Mächtikeit Unterboden nicht vorhanden");
    }

    // Skelettgehalt Oberboden (Durchschnitt)
    if (py04OBmin >= 0 && py04OBmax >= 0) {
      py04OB = (py04OBmin + py04OBmax) / 2;
    } else if (py04OBmin >= 0 && py04OBmax < 0) {
      py04OB = py04OBmin;
    } else if (py04OBmin < 0 && py04OBmax >= 0) {
      py04OB = py04OBmax;
    }
    if (py04OB < 0) {
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': GWneu - Skelettgehalt Oberboden konnte nicht berechnet werden");
    }

    // Skelettgehalt Unterboden (Durchschnitt)
    if (py04UBmin >= 0 && py04UBmax >= 0) {
      py04UB = (py04UBmin + py04UBmax) / 2;
    } else if (py04UBmin >= 0 && py04UBmax < 0) {
      py04UB = py04UBmin;
    } else if (py04UBmin < 0 && py04UBmax >= 0) {
      py04UB = py04UBmax;
    }
    if (py04UB < 0) {
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': GWneu - Skelettgehalt Unterboden konnte nicht berechnet werden");
    }

    double wsv = -1;
    if (py01 >= 0 && py02 >= 0 && py04OB >= 0 && py04UB >= 0 && wsvOben >= 0 && wsvUnten >= 0) {
      wsv = (py01 * 10 * (1 - py04OB / 100)) * (wsvOben / 100)
              + (py02 * 10 * (1 - py04UB / 100)) * (wsvUnten / 100);
    }

    // 2. Schritt: Bestimmung der Durchlässigkeit (kf-Wert) über eine Pedotransfertabelle auf Basis elementarer Parameter

    // kf Oberboden und Unterboden
    double kfOB1 = -1;
    if (bodenartOB1 == 504) {
      kfOB1 = 5;
    } else if (bodenartOB1 == 534) {
      kfOB1 = 6;
    } else if (bodenartOB1 == 202) {
      kfOB1 = 8;
    } else if (bodenartOB1 == 212) {
      kfOB1 = 11;
    } else if (bodenartOB1 == 414) {
      kfOB1 = 11;
    } else if (bodenartOB1 == 403) {
      kfOB1 = 12;
    } else if (bodenartOB1 == 332) {
      kfOB1 = 14;
    } else if (bodenartOB1 == 313) {
      kfOB1 = 16;
    } else if (bodenartOB1 == 423) {
      kfOB1 = 19;
    } else if (bodenartOB1 == 121) {
      kfOB1 = 20;
    } else if (bodenartOB1 == 231) {
      kfOB1 = 20;
    } else if (bodenartOB1 == 341) {
      kfOB1 = 48;
    } else if (bodenartOB1 == 101) {
      kfOB1 = 117;
    }

    double kfOB2 = -1;
    if (bodenartOB2 == 504) {
      kfOB2 = 5;
    } else if (bodenartOB2 == 534) {
      kfOB2 = 6;
    } else if (bodenartOB2 == 202) {
      kfOB2 = 8;
    } else if (bodenartOB2 == 212) {
      kfOB2 = 11;
    } else if (bodenartOB2 == 414) {
      kfOB2 = 11;
    } else if (bodenartOB2 == 403) {
      kfOB2 = 12;
    } else if (bodenartOB2 == 332) {
      kfOB2 = 14;
    } else if (bodenartOB2 == 313) {
      kfOB2 = 16;
    } else if (bodenartOB2 == 423) {
      kfOB2 = 19;
    } else if (bodenartOB2 == 121) {
      kfOB2 = 20;
    } else if (bodenartOB2 == 231) {
      kfOB2 = 20;
    } else if (bodenartOB2 == 341) {
      kfOB2 = 48;
    } else if (bodenartOB2 == 101) {
      kfOB2 = 117;
    }

    double kfUB1 = -1;
    if (bodenartUB1 == 504) {
      kfUB1 = 5;
    } else if (bodenartUB1 == 534) {
      kfUB1 = 6;
    } else if (bodenartUB1 == 202) {
      kfUB1 = 8;
    } else if (bodenartUB1 == 212) {
      kfUB1 = 11;
    } else if (bodenartUB1 == 414) {
      kfUB1 = 11;
    } else if (bodenartUB1 == 403) {
      kfUB1 = 12;
    } else if (bodenartUB1 == 332) {
      kfUB1 = 14;
    } else if (bodenartUB1 == 313) {
      kfUB1 = 16;
    } else if (bodenartUB1 == 423) {
      kfUB1 = 19;
    } else if (bodenartUB1 == 121) {
      kfUB1 = 20;
    } else if (bodenartUB1 == 231) {
      kfUB1 = 20;
    } else if (bodenartUB1 == 341) {
      kfUB1 = 48;
    } else if (bodenartUB1 == 101) {
      kfUB1 = 117;
    }

    double kfUB2 = -1;
    if (bodenartUB2 == 504) {
      kfUB2 = 17.5;
    } else if (bodenartUB2 == 534) {
      kfUB2 = 17.5;
    } else if (bodenartUB2 == 202) {
      kfUB2 = 18.5;
    } else if (bodenartUB2 == 403) {
      kfUB2 = 20.5;
    } else if (bodenartUB2 == 423) {
      kfUB2 = 21.5;
    } else if (bodenartUB2 == 414) {
      kfUB2 = 23;
    } else if (bodenartUB2 == 313) {
      kfUB2 = 23.5;
    } else if (bodenartUB2 == 332) {
      kfUB2 = 25;
    } else if (bodenartUB2 == 341) {
      kfUB2 = 27;
    } else if (bodenartUB2 == 231) {
      kfUB2 = 28.5;
    } else if (bodenartUB2 == 212) {
      kfUB2 = 29;
    } else if (bodenartUB2 == 121) {
      kfUB2 = 30;
    } else if (bodenartUB2 == 101) {
      kfUB2 = 30.5;
    }

    double kfOben = -1;
    if (kfOB1 >= 0 && kfOB2 >= 0) {
      kfOben = (kfOB1 + kfOB2) / 2;
    } else if (kfOB1 >= 0 && kfOB2 < 0) {
      kfOben = kfOB1;
    } else if (kfOB1 < 0 && kfOB2 >= 0) {
      kfOben = kfOB2;
    }

    double kfUnten = -1;
    if (kfUB1 >= 0 && kfUB2 >= 0) {
      kfUnten = (kfUB1 + kfUB2) / 2;
    } else if (kfUB1 >= 0 && kfUB2 < 0) {
      kfUnten = kfUB1;
    } else if (kfUB1 < 0 && kfUB2 >= 0) {
      kfUnten = kfUB2;
    }

    // Oberboden
    if (py03max == 5) {
      kfOben = 25;
    }

    if ((py04OBmin == 60 || py04OBmin == 90) && py04OBmax == 90) {
      kfOben = 300;
    } else if (py04OBmin <= 100 && py04OBmax == 100) {
      // siehe Dokumentation (keine vernünftige Aussage möglich)
      this.resultMap.put("GWNeu", "FEHLER : Aufgrund des Skelettgehaltes des OB ist keine vernünftige Aussage zur Durchlässigkeit möglich");
      return;
    } else if (py04OBmin == 100 && py04OBmax == 100) {
      kfOben = 1;
    }

    // Unterboden
    if ((py04UBmin == 60 || py04UBmin == 90) && py04UBmax == 90) {
      kfUnten = 300;
    } else if (py04UBmin <= 100 && py04UBmax == 100) {
      // siehe Dokumentation (keine vernünftige Aussage möglich)
      this.resultMap.put("GWNeu", "FEHLER : Aufgrund des Skelettgehaltes des UB ist keine vernünftige Aussage zur Durchlässigkeit möglich");
      return;
    } else if (py04UBmin <= 100 && py04UBmax == 100) {
      kfUnten = 1;
    }

    // Den kleinsten kf-Wert für die weitere Bewertung verwenden
    double kf = -1;
    if (kfUnten >= 0 && kfOben >= 0) {
      if (kfUnten < kfOben) {
        kf = kfUnten;
      } else if (kfOben < kfUnten) {
        kf = kfOben;
      }
    }

    // 3. Schritt: Gesamtbewertung Grundwasserneubildung (vgl. DS I)

    // check
    if (kf < 0 && (py10min < 0 && py10max < 0)) {
      this.resultMap.put("GWNeu", "FEHLER : kf-Wert konnte nicht berechnet werden und Durchlässigkeitsangabe fehlt. Bewertung nicht möglich");
      return;
    }
    if (wsv < 0 && (py09min < 0 && py09max < 0)) {
      this.resultMap.put("GWNeu", "FEHLER : WSV-Wert konnte nicht berechnet werden und WSV-Klasse fehlt. Bewertung nicht möglich");
      return;
    }

    if (kf >= 0) {
      // kf verwenden
      // wsv prüfen
      if (wsv >= 0) {
        // wsv verwenden
        if (kf <= 7) {
          if (wsv <= 60) {
            result = 5;
          } else if (wsv > 60 && wsv <= 90) {
            result = 4;
          } else if (wsv > 90 && wsv <= 220) {
            result = 3;
          } else if (wsv > 220) {
            result = 2;
          }
        } else if (kf > 7 && kf <= 15) {
          if (wsv <= 60) {
            result = 3;
          } else if (wsv > 60 && wsv <= 90) {
            result = 2;
          } else if (wsv > 90 && wsv <= 220) {
            result = 1;
          } else if (wsv > 220) {
            result = 1;
          }
        } else if (kf > 15 && kf <= 40) {
          if (wsv <= 60) {
            result = 5;
          } else if (wsv > 60 && wsv <= 90) {
            result = 4;
          } else if (wsv > 90 && wsv <= 220) {
            result = 3;
          } else if (wsv > 220) {
            result = 2;
          }
        } else if (kf > 40) {
          if (wsv <= 60) {
            result = 5;
          } else if (wsv > 60 && wsv <= 90) {
            result = 5;
          } else if (wsv > 90 && wsv <= 220) {
            result = 4;
          } else if (wsv > 220) {
            result = 3;
          }
        }
      } else {
        // py09 verwenden
        int resultPy09min = -1;
        int resultPy09max = -1;
        if (kf <= 7) {
          // result with py09min
          if (py09min == 5) {
            resultPy09min = 5;
          } else if (py09min == 4) {
            resultPy09min = 4;
          } else if (py09min == 3) {
            resultPy09min = 3;
          } else if (py09min == 2 || py09min == 1) {
            resultPy09min = 2;
          }
          // result with py09max
          if (py09max == 5) {
            resultPy09max = 5;
          } else if (py09max == 4) {
            resultPy09max = 4;
          } else if (py09max == 3) {
            resultPy09max = 3;
          } else if (py09max == 2 || py09max == 1) {
            resultPy09max = 2;
          }
        } else if (kf > 7 && kf <= 15) {
          // result with py09min
          if (py09min == 5) {
            resultPy09min = 3;
          } else if (py09min == 4) {
            resultPy09min = 2;
          } else if (py09min == 3) {
            resultPy09min = 1;
          } else if (py09min == 2 || py09min == 1) {
            resultPy09min = 1;
          }
          // result with py09max
          if (py09max == 5) {
            resultPy09max = 3;
          } else if (py09max == 4) {
            resultPy09max = 2;
          } else if (py09max == 3) {
            resultPy09max = 1;
          } else if (py09max == 2 || py09max == 1) {
            resultPy09max = 1;
          }
        } else if (kf > 15 && kf <= 40) {
          // result with py09min
          if (py09min == 5) {
            resultPy09min = 5;
          } else if (py09min == 4) {
            resultPy09min = 4;
          } else if (py09min == 3) {
            resultPy09min = 3;
          } else if (py09min == 2 || py09min == 1) {
            resultPy09min = 2;
          }
          // result with py09max
          if (py09max == 5) {
            resultPy09max = 5;
          } else if (py09max == 4) {
            resultPy09max = 4;
          } else if (py09max == 3) {
            resultPy09max = 3;
          } else if (py09max == 2 || py09max == 1) {
            resultPy09max = 2;
          }
        } else if (kf > 40) {
          // result with py09min
          if (py09min == 5) {
            resultPy09min = 5;
          } else if (py09min == 4) {
            resultPy09min = 5;
          } else if (py09min == 3) {
            resultPy09min = 4;
          } else if (py09min == 2 || py09min == 1) {
            resultPy09min = 3;
          }
          // result with py09max
          if (py09max == 5) {
            resultPy09max = 5;
          } else if (py09max == 4) {
            resultPy09max = 5;
          } else if (py09max == 3) {
            resultPy09max = 4;
          } else if (py09max == 2 || py09max == 1) {
            resultPy09max = 3;
          }
        }
        // Durchschnitt von resultPy09min und resultPy09max
        if (resultPy09min > 0 && resultPy09max > 0) {
          result = Double.valueOf(Math.floor((resultPy09min + resultPy09max) / 2)).intValue();
        } else if (resultPy09min > 0 && resultPy09max < 0) {
          result = resultPy09min;
        } else if (resultPy09min < 0 && resultPy09max > 0) {
          result = resultPy09max;
        }
      }
    } else if (py10min >= 0 && py10max >= 0) {
      // Durchlässigkeit verwenden (py10)

      if (wsv >= 0) {
        // wsv verwenden
        int resultPy10min = -1;
        int resultPy10max = -1;
        if (wsv <= 60) {
          // result with py10min
          if (py10min == 5) {
            resultPy10min = 5;
          } else if (py10min == 4) {
            resultPy10min = 3;
          } else if (py10min == 3) {
            resultPy10min = 5;
          } else if (py10min == 2 || py10min == 1) {
            resultPy10min = 5;
          }
          // result with py10max
          if (py10max == 5) {
            resultPy10max = 5;
          } else if (py10max == 4) {
            resultPy10max = 3;
          } else if (py10max == 3) {
            resultPy10max = 5;
          } else if (py10max == 2 || py10max == 1) {
            resultPy10max = 5;
          }
        } else if (wsv > 60 && wsv <= 90) {
          // result with py10min
          if (py10min == 5) {
            resultPy10min = 4;
          } else if (py10min == 4) {
            resultPy10min = 2;
          } else if (py10min == 3) {
            resultPy10min = 4;
          } else if (py10min == 2 || py10min == 1) {
            resultPy10min = 5;
          }
          // result with py10max
          if (py10max == 5) {
            resultPy10max = 4;
          } else if (py10max == 4) {
            resultPy10max = 2;
          } else if (py10max == 3) {
            resultPy10max = 4;
          } else if (py10max == 2 || py10max == 1) {
            resultPy10max = 5;
          }
        } else if (wsv > 90 && wsv <= 220) {
          // result with py10min
          if (py10min == 5) {
            resultPy10min = 3;
          } else if (py10min == 4) {
            resultPy10min = 1;
          } else if (py10min == 3) {
            resultPy10min = 3;
          } else if (py10min == 2 || py10min == 1) {
            resultPy10min = 4;
          }
          // result with py10max
          if (py10max == 5) {
            resultPy10max = 3;
          } else if (py10max == 4) {
            resultPy10max = 1;
          } else if (py10max == 3) {
            resultPy10max = 3;
          } else if (py10max == 2 || py10max == 1) {
            resultPy10max = 4;
          }
        } else if (wsv > 220) {
          // result with py10min
          if (py10min == 5) {
            resultPy10min = 2;
          } else if (py10min == 4) {
            resultPy10min = 1;
          } else if (py10min == 3) {
            resultPy10min = 2;
          } else if (py10min == 2 || py10min == 1) {
            resultPy10min = 3;
          }
          // result with py10max
          if (py10max == 5) {
            resultPy10max = 2;
          } else if (py10max == 4) {
            resultPy10max = 1;
          } else if (py10max == 3) {
            resultPy10max = 2;
          } else if (py10max == 2 || py10max == 1) {
            resultPy10max = 3;
          }
        }
        // Durchschnitt von resultPy10min und resultPy10max
        if (resultPy10min > 0 && resultPy10max > 0) {
          result = Double.valueOf(Math.floor((resultPy10min + resultPy10max) / 2)).intValue();
        } else if (resultPy10min > 0 && resultPy10max < 0) {
          result = resultPy10min;
        } else if (resultPy10min < 0 && resultPy10max > 0) {
          result = resultPy10max;
        }
      } else {
        // hier wird's kompliziert, da wir 4 Werte haben, jeweils ein max und ein min Wert (Py10, Py09)
        // int resultPY10PY09 -->
        int resultMinMin = -1; // Py10 Min und Py09 Min
        int resultMinMax = -1; // Py10 Min und Py09 Max
        int resultMaxMin = -1; // Py10 Max und Py09 Min
        int resultMaxMax = -1; // Py10 Max und Py09 Max

        // PY10 MIN
        if (py10min == 5) {
          // result with py09min
          if (py09min == 5) {
            resultMinMin = 5;
          } else if (py09min == 4) {
            resultMinMin = 4;
          } else if (py09min == 3) {
            resultMinMin = 3;
          } else if (py09min == 2 || py09min == 1) {
            resultMinMin = 2;
          }
          // result with py09max
          if (py09max == 5) {
            resultMinMax = 5;
          } else if (py09max == 4) {
            resultMinMax = 4;
          } else if (py09max == 3) {
            resultMinMax = 3;
          } else if (py09max == 2 || py09max == 1) {
            resultMinMax = 2;
          }
        } else if (py10min == 4) {
          // result with py09min
          if (py09min == 5) {
            resultMinMin = 3;
          } else if (py09min == 4) {
            resultMinMin = 2;
          } else if (py09min == 3) {
            resultMinMin = 1;
          } else if (py09min == 2 || py09min == 1) {
            resultMinMin = 1;
          }
          // result with py09max
          if (py09max == 5) {
            resultMinMax = 3;
          } else if (py09max == 4) {
            resultMinMax = 2;
          } else if (py09max == 3) {
            resultMinMax = 1;
          } else if (py09max == 2 || py09max == 1) {
            resultMinMax = 1;
          }
        } else if (py10min == 3) {
          // result with py09min
          if (py09min == 5) {
            resultMinMin = 5;
          } else if (py09min == 4) {
            resultMinMin = 4;
          } else if (py09min == 3) {
            resultMinMin = 3;
          } else if (py09min == 2 || py09min == 1) {
            resultMinMin = 2;
          }
          // result with py09max
          if (py09max == 5) {
            resultMinMax = 5;
          } else if (py09max == 4) {
            resultMinMax = 4;
          } else if (py09max == 3) {
            resultMinMax = 3;
          } else if (py09max == 2 || py09max == 1) {
            resultMinMax = 2;
          }
        } else if (py10min == 2 || py10min == 1) {
          // result with py09min
          if (py09min == 5) {
            resultMinMin = 5;
          } else if (py09min == 4) {
            resultMinMin = 5;
          } else if (py09min == 3) {
            resultMinMin = 4;
          } else if (py09min == 2 || py09min == 1) {
            resultMinMin = 3;
          }
          // result with py09max
          if (py09max == 5) {
            resultMinMax = 5;
          } else if (py09max == 4) {
            resultMinMax = 5;
          } else if (py09max == 3) {
            resultMinMax = 4;
          } else if (py09max == 2 || py09max == 1) {
            resultMinMax = 3;
          }
        }

        // PY10 MAX
        if (py10max == 5) {
          // result with py09min
          if (py09min == 5) {
            resultMaxMin = 5;
          } else if (py09min == 4) {
            resultMaxMin = 4;
          } else if (py09min == 3) {
            resultMaxMin = 3;
          } else if (py09min == 2 || py09min == 1) {
            resultMaxMin = 2;
          }
          // result with py09max
          if (py09max == 5) {
            resultMaxMax = 5;
          } else if (py09max == 4) {
            resultMaxMax = 4;
          } else if (py09max == 3) {
            resultMaxMax = 3;
          } else if (py09max == 2 || py09max == 1) {
            resultMaxMax = 2;
          }
        } else if (py10max == 4) {
          // result with py09min
          if (py09min == 5) {
            resultMaxMin = 3;
          } else if (py09min == 4) {
            resultMaxMin = 2;
          } else if (py09min == 3) {
            resultMaxMin = 1;
          } else if (py09min == 2 || py09min == 1) {
            resultMaxMin = 1;
          }
          // result with py09max
          if (py09max == 5) {
            resultMaxMax = 3;
          } else if (py09max == 4) {
            resultMaxMax = 2;
          } else if (py09max == 3) {
            resultMaxMax = 1;
          } else if (py09max == 2 || py09max == 1) {
            resultMaxMax = 1;
          }
        } else if (py10max == 3) {
          // result with py09min
          if (py09min == 5) {
            resultMaxMin = 5;
          } else if (py09min == 4) {
            resultMaxMin = 4;
          } else if (py09min == 3) {
            resultMaxMin = 3;
          } else if (py09min == 2 || py09min == 1) {
            resultMaxMin = 2;
          }
          // result with py09max
          if (py09max == 5) {
            resultMaxMax = 5;
          } else if (py09max == 4) {
            resultMaxMax = 4;
          } else if (py09max == 3) {
            resultMaxMax = 3;
          } else if (py09max == 2 || py09max == 1) {
            resultMaxMax = 2;
          }
        } else if (py10max == 2 || py10max == 1) {
          // result with py09min
          if (py09min == 5) {
            resultMaxMin = 5;
          } else if (py09min == 4) {
            resultMaxMin = 5;
          } else if (py09min == 3) {
            resultMaxMin = 4;
          } else if (py09min == 2 || py09min == 1) {
            resultMaxMin = 3;
          }
          // result with py09max
          if (py09max == 5) {
            resultMaxMax = 5;
          } else if (py09max == 4) {
            resultMaxMax = 5;
          } else if (py09max == 3) {
            resultMaxMax = 4;
          } else if (py09max == 2 || py09max == 1) {
            resultMaxMax = 3;
          }
        }

        // Durchschnitt aller vier Werte berechnen
        int[] resultArr = new int[]{resultMinMin, resultMinMax, resultMaxMin, resultMaxMax};

        int count = 0;
        int sum = 0;
        for (Integer val : resultArr) {
          if (val > 0) {
            sum += val;
            ++count;
          }
        }
        if (sum > 0) {
          double durchschnitt = sum / count;
          result = Double.valueOf(Math.floor(durchschnitt)).intValue();
        }
      }
    }

    // Modifikation bei grundwassernahen Böden
    long bodentypkurz = ((KeyAttribute) this.bk.getAttribute("Py07_Typ")).getId();
    if (bodentypkurz == 1) {
      // Moore (Py07 = MO)
      result = 5;
    } else if (bodentypkurz == 2) {
      // Grundwasserbeeinflusste Böden (Py07 = G)
      if (result == 1 || result == 2) {
        result = 4;
      } else if (result == 3 || result == 4 || result == 5) {
        result = 5;
      }
    }

    if (result != null) {
      this.bk.setAttributeValue("GWneu", String.valueOf(result));
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': 1C.3 saved with value '" + result + "'");
      this.resultMap.put("GWneu", String.valueOf(result));
    } else {
      this.resultMap.put("GWneu", "FEHLER : Konnte nicht ermittelt werden");
    }
  }

  public void calculate1C5() throws Exception {
    Integer result = null;

    long nutzung = ((KeyAttribute) this.bk.getAttribute("Landnutzung")).getId();
    long py03min = ((KeyAttribute) this.bk.getAttribute("Py03_Hu_min")).getId();
    long py03max = ((KeyAttribute) this.bk.getAttribute("Py03_Hu_max")).getId();
    long py04OBmin = ((KeyAttribute) this.bk.getAttribute("Py04_OB_Sk_min")).getId();
    long py04OBmax = ((KeyAttribute) this.bk.getAttribute("Py04_OB_Sk_max")).getId();
    long py04OB = -1;
    long py04UBmin = ((KeyAttribute) this.bk.getAttribute("Py04_UB_Sk_min")).getId();
    long py04UBmax = ((KeyAttribute) this.bk.getAttribute("Py04_UB_Sk_max")).getId();
    long py04UB = -1;

    // Skelett Oberboden
    if (py04OBmin >= 0 && py04OBmax >= 0) {
      py04OB = (py04OBmin + py04OBmax) / 2;
    } else if (py04OBmin >= 0 && py04OBmax < 0) {
      py04OB = py04OBmin;
    } else if (py04OBmin < 0 && py04OBmax >= 0) {
      py04OB = py04OBmax;
    }
    // Skelett Unterboden
    if (py04UBmin >= 0 && py04UBmax >= 0) {
      py04UB = (py04UBmin + py04UBmax) / 2;
    } else if (py04UBmin >= 0 && py04UBmax < 0) {
      py04UB = py04UBmin;
    } else if (py04UBmin < 0 && py04UBmax >= 0) {
      py04UB = py04UBmax;
    }

    // check
    if (py03min < 0 && py03max < 0) {
      this.resultMap.put("CO2_Senke", "FEHLER : Humusgehalt nicht vorhanden");
      return;
    }
    if (py04OB < 0) {
      this.resultMap.put("CO2_Senke", "FEHLER : Skelettgehalt für Oberboden nicht vorhanden");
      return;
    }
    if (py04UB < 0) {
      this.resultMap.put("CO2_Senke", "FEHLER : Skelettgehalt für Unterboden nicht vorhanden");
      return;
    }

    //  BEWERTUNG

    // Vier Werte HumusMin, HumusMax, SkelettMin, SkelettMax
    int resultMinMin = -1;
    int resultMinMax = -1;
    int resultMaxMin = -1;
    int resultMaxMax = -1;


    // Humus Min
    if (py03min == 5) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        resultMinMin = 1;
      } else if (py04OBmin == 15) {
        // Nothing
      } else if (py04OBmin == 30) {
        // Nothing
      } else if (py04OBmin == 60) {
        // Nothing
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        // Nothing
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        resultMinMax = 1;
      } else if (py04OBmax == 15) {
        // Nothing
      } else if (py04OBmax == 30) {
        // Nothing
      } else if (py04OBmax == 60) {
        // Nothing
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        // Nothing
      }
    } else if (py03min == 4) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        resultMinMin = 2;
      } else if (py04OBmin == 15) {
        resultMinMin = 2;
      } else if (py04OBmin == 30) {
        resultMinMin = 3;
      } else if (py04OBmin == 60) {
        resultMinMin = 3;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        resultMinMin = 5;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        resultMinMax = 2;
      } else if (py04OBmax == 15) {
        resultMinMax = 2;
      } else if (py04OBmax == 30) {
        resultMinMax = 3;
      } else if (py04OBmax == 60) {
        resultMinMax = 3;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        resultMinMax = 5;
      }
    } else if (py03min == 3) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        resultMinMin = 3;
      } else if (py04OBmin == 15) {
        resultMinMin = 3;
      } else if (py04OBmin == 30) {
        resultMinMin = 4;
      } else if (py04OBmin == 60) {
        resultMinMin = 4;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        resultMinMin = 5;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        resultMinMax = 3;
      } else if (py04OBmax == 15) {
        resultMinMax = 3;
      } else if (py04OBmax == 30) {
        resultMinMax = 4;
      } else if (py04OBmax == 60) {
        resultMinMax = 4;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        resultMinMax = 5;
      }
    } else if (py03min == 2) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        resultMinMin = 4;
      } else if (py04OBmin == 15) {
        resultMinMin = 4;
      } else if (py04OBmin == 30) {
        resultMinMin = 4;
      } else if (py04OBmin == 60) {
        resultMinMin = 5;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        resultMinMin = 5;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        resultMinMax = 4;
      } else if (py04OBmax == 15) {
        resultMinMax = 4;
      } else if (py04OBmax == 30) {
        resultMinMax = 4;
      } else if (py04OBmax == 60) {
        resultMinMax = 5;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        resultMinMax = 5;
      }
    } else if (py03min == 1 || py03min == 0) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        resultMinMin = 5;
      } else if (py04OBmin == 15) {
        resultMinMin = 5;
      } else if (py04OBmin == 30) {
        resultMinMin = 5;
      } else if (py04OBmin == 60) {
        resultMinMin = 5;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        resultMinMin = 5;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        resultMinMax = 5;
      } else if (py04OBmax == 15) {
        resultMinMax = 5;
      } else if (py04OBmax == 30) {
        resultMinMax = 5;
      } else if (py04OBmax == 60) {
        resultMinMax = 5;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        resultMinMax = 5;
      }
    }

    // Humus Max
    if (py03max == 5) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        resultMaxMin = 1;
      } else if (py04OBmin == 15) {
        // Nothing
      } else if (py04OBmin == 30) {
        // Nothing
      } else if (py04OBmin == 60) {
        // Nothing
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        // Nothing
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        resultMaxMax = 1;
      } else if (py04OBmax == 15) {
        // Nothing
      } else if (py04OBmax == 30) {
        // Nothing
      } else if (py04OBmax == 60) {
        // Nothing
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        // Nothing
      }
    } else if (py03max == 4) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        resultMaxMin = 2;
      } else if (py04OBmin == 15) {
        resultMaxMin = 2;
      } else if (py04OBmin == 30) {
        resultMaxMin = 3;
      } else if (py04OBmin == 60) {
        resultMaxMin = 3;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        resultMaxMin = 5;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        resultMaxMax = 2;
      } else if (py04OBmax == 15) {
        resultMaxMax = 2;
      } else if (py04OBmax == 30) {
        resultMaxMax = 3;
      } else if (py04OBmax == 60) {
        resultMaxMax = 3;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        resultMaxMax = 5;
      }
    } else if (py03max == 3) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        resultMaxMin = 3;
      } else if (py04OBmin == 15) {
        resultMaxMin = 3;
      } else if (py04OBmin == 30) {
        resultMaxMin = 4;
      } else if (py04OBmin == 60) {
        resultMaxMin = 4;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        resultMaxMin = 5;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        resultMaxMax = 3;
      } else if (py04OBmax == 15) {
        resultMaxMax = 3;
      } else if (py04OBmax == 30) {
        resultMaxMax = 4;
      } else if (py04OBmax == 60) {
        resultMaxMax = 4;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        resultMaxMax = 5;
      }
    } else if (py03max == 2) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        resultMaxMin = 4;
      } else if (py04OBmin == 15) {
        resultMaxMin = 4;
      } else if (py04OBmin == 30) {
        resultMaxMin = 4;
      } else if (py04OBmin == 60) {
        resultMaxMin = 5;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        resultMaxMin = 5;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        resultMaxMax = 4;
      } else if (py04OBmax == 15) {
        resultMaxMax = 4;
      } else if (py04OBmax == 30) {
        resultMaxMax = 4;
      } else if (py04OBmax == 60) {
        resultMaxMax = 5;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        resultMaxMax = 5;
      }
    } else if (py03max == 1 || py03max == 0) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        resultMaxMin = 5;
      } else if (py04OBmin == 15) {
        resultMaxMin = 5;
      } else if (py04OBmin == 30) {
        resultMaxMin = 5;
      } else if (py04OBmin == 60) {
        resultMaxMin = 5;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        resultMaxMin = 5;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        resultMaxMax = 5;
      } else if (py04OBmax == 15) {
        resultMaxMax = 5;
      } else if (py04OBmax == 30) {
        resultMaxMax = 5;
      } else if (py04OBmax == 60) {
        resultMaxMax = 5;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        resultMaxMax = 5;
      }
    }

    // Durchschnitt aller vier Werte berechnen
    int[] resultArr = new int[]{resultMinMin, resultMinMax, resultMaxMin, resultMaxMax};

    int count = 0;
    int sum = 0;
    for (Integer val : resultArr) {
      if (val > 0) {
        sum += val;
        ++count;
      }
    }
    if (sum > 0) {
      double durchschnitt = sum / count;
      result = Double.valueOf(Math.floor(durchschnitt)).intValue();
    }

    // Korrektur nach Nutzung
    if (nutzung == 100 && nutzung == 950) {
      result = 1;
    }

    if (result != null) {
      this.bk.setAttributeValue("CO2_Senke", String.valueOf(result));
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': 1C.5 saved with value '" + result + "'");
      this.resultMap.put("CO2_Senke", String.valueOf(result));
    } else {
      this.resultMap.put("CO2_Senke", "FEHLER : Konnte nicht ermittelt werden");
    }
  }

  public void calculate1D1() throws Exception {
    Integer result = null;

    float py01 = this.bk.getAttributeValueFloat("Py01_OB_cm");
    float py02 = this.bk.getAttributeValueFloat("Py02_UB_cm");
    long bodenartOB1 = ((KeyAttribute) this.bk.getAttribute("Bodenart_OB_1")).getId();
    long bodenartOB2 = ((KeyAttribute) this.bk.getAttribute("Bodenart_OB_2")).getId();
    long bodenartUB1 = ((KeyAttribute) this.bk.getAttribute("Bodenart_UB_1")).getId();
    long bodenartUB2 = ((KeyAttribute) this.bk.getAttribute("Bodenart_UB_2")).getId();
    long nutzung = ((KeyAttribute) this.bk.getAttribute("Landnutzung")).getId();
    long py03min = ((KeyAttribute) this.bk.getAttribute("Py03_Hu_min")).getId();
    long py03max = ((KeyAttribute) this.bk.getAttribute("Py03_Hu_max")).getId();
    long py04OBmin = ((KeyAttribute) this.bk.getAttribute("Py04_OB_Sk_min")).getId();
    long py04OBmax = ((KeyAttribute) this.bk.getAttribute("Py04_OB_Sk_max")).getId();
    long py04OB = -1;
    long py04UBmin = ((KeyAttribute) this.bk.getAttribute("Py04_UB_Sk_min")).getId();
    long py04UBmax = ((KeyAttribute) this.bk.getAttribute("Py04_UB_Sk_max")).getId();
    long py04UB = -1;
    long py06OBmin = ((KeyAttribute) this.bk.getAttribute("Py06_OB_pH_min")).getId();
    long py06OBmax = ((KeyAttribute) this.bk.getAttribute("Py06_OB_pH_max")).getId();
    double py06OB = -1;
    long py06UBmin = ((KeyAttribute) this.bk.getAttribute("Py06_UB_pH_min")).getId();
    long py06UBmax = ((KeyAttribute) this.bk.getAttribute("Py06_UB_pH_max")).getId();
    double py06UB = -1;


    // pH-Wert Oberboden
    if (py06OBmin >= 0 && py06OBmax >= 0) {
      py06OB = (py06OBmin + py06OBmax) / 2;
    } else if (py06OBmin >= 0 && py06OBmax < 0) {
      py06OB = py06OBmin;
    } else if (py06OBmin < 0 && py06OBmax >= 0) {
      py06OB = py06OBmax;
    }

    // pH-Wert Unterboden
    if (py06UBmin >= 0 && py06UBmax >= 0) {
      py06UB = (py06UBmin + py06UBmax) / 2;
    } else if (py06UBmin >= 0 && py06UBmax < 0) {
      py06UB = py06UBmin;
    } else if (py06UBmin < 0 && py06UBmax >= 0) {
      py06UB = py06UBmax;
    }

    // Skelett Oberboden
    if (py04OBmin >= 0 && py04OBmax >= 0) {
      py04OB = (py04OBmin + py04OBmax) / 2;
    } else if (py04OBmin >= 0 && py04OBmax < 0) {
      py04OB = py04OBmin;
    } else if (py04OBmin < 0 && py04OBmax >= 0) {
      py04OB = py04OBmax;
    }
    // Skelett Unterboden
    if (py04UBmin >= 0 && py04UBmax >= 0) {
      py04UB = (py04UBmin + py04UBmax) / 2;
    } else if (py04UBmin >= 0 && py04UBmax < 0) {
      py04UB = py04UBmin;
    } else if (py04UBmin < 0 && py04UBmax >= 0) {
      py04UB = py04UBmax;
    }

    // check
    if (py03min < 0 && py03max < 0) {
      this.resultMap.put("FiltPuff", "FEHLER : Humusgehalt nicht vorhanden");
      return;
    }
    if (py04OB < 0 && py04UB < 0) {
      this.resultMap.put("FiltPuff", "FEHLER : Skelettgehalt nicht vorhanden");
      return;
    }
    if (py06OB < 0 || py06UB < 0) {
      this.resultMap.put("FiltPuff", "FEHLER : pH-Wert (Bodenreaktion) für Oberboden oder Unterboden nicht vorhanden");
      return;
    }

    if (py03min < 0 || py03max < 0) {
      this.resultMap.put("FiltPuff", "FEHLER : Humusgehalt für Oberboden oder Unterboden nicht vorhanden");
      return;
    }
    if (bodenartOB1 < 0) {
      this.resultMap.put("FiltPuff", "FEHLER : Bodenart für Oberboden nicht vorhanden");
      return;
    }
    if (bodenartUB1 < 0) {
      this.resultMap.put("FiltPuff", "FEHLER : Bodenart für Unterboden nicht vorhanden");
      return;
    }
    if (py01 < 0) {
      this.resultMap.put("FiltPuff", "FEHLER : Mächtigkeit für Oberboden nicht vorhanden");
      return;
    }
    if (py02 < 0) {
      this.resultMap.put("FiltPuff", "FEHLER : Mächtigkeit für Unterboden nicht vorhanden");
      return;
    }

    // 1. Schritt: Bestimmung der Bindungsstärke (für Cd) in Abhängigkeit vom pH-Wert

    // Bindung OB und Bindung UB = Py06 (siehe Dokumentation)
    double bindungOben = py06OB;
    double bindungUnten = py06UB;

    // Anpassung Oberboden nach Humusgehalt
    double corrMin = -1;
    if (py03min == 0 || py03min == 1) {
      corrMin = 0.0;
    } else if (py03min == 2) {
      corrMin = 0.5;
    } else if (py03min == 3) {
      corrMin = 1.0;
    } else if (py03min == 4 || py03min == 5) {
      corrMin = 1.5;
    }

    double corrMax = -1;
    if (py03max == 0 || py03max == 1) {
      corrMax = 0.0;
    } else if (py03max == 2) {
      corrMax = 0.5;
    } else if (py03max == 3) {
      corrMax = 1.0;
    } else if (py03max == 4 || py03max == 5) {
      corrMax = 1.5;
    }

    // Durchschnitt 
    double corrHumus = 0;
    if (corrMin >= 0 && corrMax >= 0) {
      corrHumus = (corrMin + corrMax) / 2;
    } else if (corrMin >= 0 && corrMax < 0) {
      corrHumus = corrMin;
    } else if (corrMin < 0 && corrMax >= 0) {
      corrHumus = corrMax;
    }

    bindungOben = bindungOben + corrHumus;

    // 3. Schritt: Modifikation nach Tongehalt

    double modOB1 = -1;
    double modOB2 = -1;
    double modUB1 = -1;
    double modUB2 = -1;

    if (bodenartOB1 == 101 || bodenartOB1 == 121 || bodenartOB1 == 202 || bodenartOB1 == 212
            || bodenartOB1 == 231 || bodenartOB1 == 332) {
      modOB1 = 0;
    } else if (bodenartOB1 == 313 || bodenartOB1 == 341 || bodenartOB1 == 403 || bodenartOB1 == 414
            || bodenartOB1 == 423 || bodenartOB1 == 504 || bodenartOB1 == 534) {
      modOB1 = 0.5;
    }
    if (bodenartOB2 == 101 || bodenartOB2 == 121 || bodenartOB2 == 202 || bodenartOB2 == 212
            || bodenartOB2 == 231 || bodenartOB2 == 332) {
      modOB2 = 0;
    } else if (bodenartOB2 == 313 || bodenartOB2 == 341 || bodenartOB2 == 403 || bodenartOB2 == 414
            || bodenartOB2 == 423 || bodenartOB2 == 504 || bodenartOB2 == 534) {
      modOB2 = 0.5;
    }
    if (bodenartUB1 == 101 || bodenartUB1 == 121 || bodenartUB1 == 202 || bodenartUB1 == 212
            || bodenartUB1 == 231 || bodenartUB1 == 332) {
      modUB1 = 0;
    } else if (bodenartUB1 == 313 || bodenartUB1 == 341 || bodenartUB1 == 403 || bodenartUB1 == 414
            || bodenartUB1 == 423 || bodenartUB1 == 504 || bodenartUB1 == 534) {
      modUB1 = 0.5;
    }
    if (bodenartUB2 == 101 || bodenartUB2 == 121 || bodenartUB2 == 202 || bodenartUB2 == 212
            || bodenartUB2 == 231 || bodenartUB2 == 332) {
      modUB2 = 0;
    } else if (bodenartUB2 == 313 || bodenartUB2 == 341 || bodenartUB2 == 403 || bodenartUB2 == 414
            || bodenartUB2 == 423 || bodenartUB2 == 504 || bodenartUB2 == 534) {
      modUB2 = 0.5;
    }

    if (modOB1 >= 0 && modOB2 >= 0) {
      bindungOben = bindungOben + (modOB1 + modOB2) / 2;
    } else if (modOB1 >= 0 && modOB2 < 0) {
      bindungOben = bindungOben + modOB1;
    } else if (modOB1 < 0 && modOB2 >= 0) {
      bindungOben = bindungOben + modOB2;
    }

    if (modUB1 >= 0 && modUB2 >= 0) {
      bindungUnten = bindungUnten + (modUB1 + modUB2) / 2;
    } else if (modUB1 >= 0 && modUB2 < 0) {
      bindungUnten = bindungUnten + modUB1;
    } else if (modUB1 < 0 && modUB2 >= 0) {
      bindungUnten = bindungUnten + modUB2;
    }

    // 4. Schritt: Gesamtbewertung (reduziert auf den Feinboden)

    double bindungRel = py01 / 100 * bindungOben * (1 - py04OB / 100)
            + py02 / 100 * bindungUnten * (1 - py04UB / 100);

    if (bindungRel < 1.5) {
      result = 5;
    } else if (bindungRel >= 1.5 && bindungRel < 2.5) {
      result = 4;
    } else if (bindungRel >= 2.5 && bindungRel < 3.5) {
      result = 3;
    } else if (bindungRel >= 3.5 && bindungRel < 4.5) {
      result = 2;
    } else if (bindungRel >= 4.5) {
      result = 1;
    }

    if (result != null) {
      // save to horizont
      this.bk.setAttributeValue("FiltPuff", String.valueOf(result));
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': 1D.1 saved with value '" + result + "'");
      this.resultMap.put("FiltPuff", String.valueOf(result));
    } else {
      this.resultMap.put("FiltPuff", "FEHLER : Konnte nicht ermittelt werden");
    }
  }

  public void calculate1D2() throws Exception {
    Integer result = null;

    long bodenartOB1 = ((KeyAttribute) this.bk.getAttribute("Bodenart_OB_1")).getId();
    long bodenartOB2 = ((KeyAttribute) this.bk.getAttribute("Bodenart_OB_2")).getId();
    long py04OBmin = ((KeyAttribute) this.bk.getAttribute("Py04_OB_Sk_min")).getId();
    long py04OBmax = ((KeyAttribute) this.bk.getAttribute("Py04_OB_Sk_max")).getId();
    long py03min = ((KeyAttribute) this.bk.getAttribute("Py03_Hu_min")).getId();
    long py03max = ((KeyAttribute) this.bk.getAttribute("Py03_Hu_max")).getId();
    long py03a = ((KeyAttribute) this.bk.getAttribute("Py03a_Hf")).getId();
    long py06OBmin = ((KeyAttribute) this.bk.getAttribute("Py06_OB_pH_min")).getId();
    long py06OBmax = ((KeyAttribute) this.bk.getAttribute("Py06_OB_pH_max")).getId();
    int py06OB = -1;

    if (py03min < 0 && py03max < 0) {
      this.resultMap.put("Transform", "FEHLER : angabe zum Humusgehalt nicht vorhanden");
      return;
    }

    if (py03a < 0) {
      this.resultMap.put("Transform", "FEHLER : Angabe zur Humusform nicht vorhanden");
      return;
    }

    // pH Oberboden
    if (py06OBmin >= 0 && py06OBmax >= 0) {
      double average = (py06OBmin + py06OBmax) / 2;
      if (average == 2.5) {
        py06OB = Double.valueOf(Math.floor(average)).intValue();
      } else {
        py06OB = Long.valueOf(Math.round(average)).intValue();
      }
    } else if (py06OBmin >= 0 && py06OBmax < 0) {
      py06OB = Long.valueOf(py06OBmin).intValue();
    } else if (py06OBmin < 0 && py06OBmax >= 0) {
      py06OB = Long.valueOf(py06OBmax).intValue();
    }

    // 1. Schritt: Einschätzung der mikrobiellen Abbauleistung nach Humusform und pH-Wert des Oberbodens

    int level = -1;

    if ((py03a == 6)
            || (py03a == 8 && (py06OB == 1 || py06OB == 2))
            || (py03a == 3 && (py06OB == 1 || py06OB == 2))) {
      level = 1;
    } else if ((py03a == 1)
            || (py03a == 4)
            || (py03a == 5)
            || (py03a == 2 && (py06OB == 1 || py06OB == 2))
            || (py03a == 8 && (py06OB == 3 || py06OB == 4 || py06OB == 5))
            || (py03a == 3 && (py06OB == 3 || py06OB == 4 || py06OB == 5))
            || (py03a == 0 && (py06OB == 1 || py06OB == 2))) {
      level = 2;
    } else if ((py03a == 2)
            || (py03a == 7)
            || (py03a == 0 && (py06OB == 3 || py06OB == 4 || py06OB == 5))) {
      level = 3;
    }

    // 2. Schritt: Modifikation des Tongehalts in Abhängigkeit vom Skelettgehalt

    // Vier Werte bodenart1, bodenart2, skelettOB1, SkelettOB2
    double tonB1Min = -1;
    double tonB1Max = -1;
    double tonB2Min = -1;
    double tonB2Max = -1;

    // Bodenart 1
    if (bodenartOB1 == 101) {
      // S
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB1Min = 2.5;
      } else if (py04OBmin == 15) {
        tonB1Min = 2;
      } else if (py04OBmin == 30) {
        tonB1Min = 2;
      } else if (py04OBmin == 60) {
        tonB1Min = 1;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB1Min = 0;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB1Max = 2.5;
      } else if (py04OBmax == 15) {
        tonB1Max = 2;
      } else if (py04OBmax == 30) {
        tonB1Max = 2;
      } else if (py04OBmax == 60) {
        tonB1Max = 1;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB1Max = 0;
      }
    } else if (bodenartOB1 == 121) {
      // uS
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB1Min = 2.5;
      } else if (py04OBmin == 15) {
        tonB1Min = 2;
      } else if (py04OBmin == 30) {
        tonB1Min = 2;
      } else if (py04OBmin == 60) {
        tonB1Min = 1;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB1Min = 0;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB1Max = 2.5;
      } else if (py04OBmax == 15) {
        tonB1Max = 2;
      } else if (py04OBmax == 30) {
        tonB1Max = 2;
      } else if (py04OBmax == 60) {
        tonB1Max = 1;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB1Max = 0;
      }
    } else if (bodenartOB1 == 212) {
      // sU
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB1Min = 7.5;
      } else if (py04OBmin == 15) {
        tonB1Min = 6;
      } else if (py04OBmin == 30) {
        tonB1Min = 5;
      } else if (py04OBmin == 60) {
        tonB1Min = 3;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB1Min = 1;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB1Max = 7.5;
      } else if (py04OBmax == 15) {
        tonB1Max = 6;
      } else if (py04OBmax == 30) {
        tonB1Max = 5;
      } else if (py04OBmax == 60) {
        tonB1Max = 3;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB1Max = 1;
      }
    } else if (bodenartOB1 == 202) {
      // U
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB1Min = 10;
      } else if (py04OBmin == 15) {
        tonB1Min = 9;
      } else if (py04OBmin == 30) {
        tonB1Min = 7;
      } else if (py04OBmin == 60) {
        tonB1Min = 4;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB1Min = 1;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB1Max = 10;
      } else if (py04OBmax == 15) {
        tonB1Max = 9;
      } else if (py04OBmax == 30) {
        tonB1Max = 7;
      } else if (py04OBmax == 60) {
        tonB1Max = 4;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB1Max = 1;
      }
    } else if (bodenartOB1 == 231) {
      // lS
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB1Min = 10;
      } else if (py04OBmin == 15) {
        tonB1Min = 9;
      } else if (py04OBmin == 30) {
        tonB1Min = 7;
      } else if (py04OBmin == 60) {
        tonB1Min = 4;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB1Min = 1;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB1Max = 10;
      } else if (py04OBmax == 15) {
        tonB1Max = 9;
      } else if (py04OBmax == 30) {
        tonB1Max = 7;
      } else if (py04OBmax == 60) {
        tonB1Max = 4;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB1Max = 1;
      }
    } else if (bodenartOB1 == 332) {
      // lU
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB1Min = 20;
      } else if (py04OBmin == 15) {
        tonB1Min = 17;
      } else if (py04OBmin == 30) {
        tonB1Min = 14;
      } else if (py04OBmin == 60) {
        tonB1Min = 8;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB1Min = 2;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB1Max = 20;
      } else if (py04OBmax == 15) {
        tonB1Max = 17;
      } else if (py04OBmax == 30) {
        tonB1Max = 14;
      } else if (py04OBmax == 60) {
        tonB1Max = 8;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB1Max = 2;
      }
    } else if (bodenartOB1 == 342) {
      // tS
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB1Min = 20;
      } else if (py04OBmin == 15) {
        tonB1Min = 17;
      } else if (py04OBmin == 30) {
        tonB1Min = 14;
      } else if (py04OBmin == 60) {
        tonB1Min = 8;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB1Min = 2;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB1Max = 20;
      } else if (py04OBmax == 15) {
        tonB1Max = 17;
      } else if (py04OBmax == 30) {
        tonB1Max = 14;
      } else if (py04OBmax == 60) {
        tonB1Max = 8;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB1Max = 2;
      }
    } else if (bodenartOB1 == 313) {
      // sL
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB1Min = 20;
      } else if (py04OBmin == 15) {
        tonB1Min = 17;
      } else if (py04OBmin == 30) {
        tonB1Min = 14;
      } else if (py04OBmin == 60) {
        tonB1Min = 8;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB1Min = 2;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB1Max = 20;
      } else if (py04OBmax == 15) {
        tonB1Max = 17;
      } else if (py04OBmax == 30) {
        tonB1Max = 14;
      } else if (py04OBmax == 60) {
        tonB1Max = 8;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB1Max = 2;
      }
    } else if (bodenartOB1 == 423) {
      // uL
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB1Min = 30;
      } else if (py04OBmin == 15) {
        tonB1Min = 25;
      } else if (py04OBmin == 30) {
        tonB1Min = 21;
      } else if (py04OBmin == 60) {
        tonB1Min = 12;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB1Min = 3;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB1Max = 30;
      } else if (py04OBmax == 15) {
        tonB1Max = 25;
      } else if (py04OBmax == 30) {
        tonB1Max = 21;
      } else if (py04OBmax == 60) {
        tonB1Max = 12;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB1Max = 3;
      }
    } else if (bodenartOB1 == 403) {
      // L
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB1Min = 30;
      } else if (py04OBmin == 15) {
        tonB1Min = 25;
      } else if (py04OBmin == 30) {
        tonB1Min = 21;
      } else if (py04OBmin == 60) {
        tonB1Min = 12;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB1Min = 3;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB1Max = 30;
      } else if (py04OBmax == 15) {
        tonB1Max = 25;
      } else if (py04OBmax == 30) {
        tonB1Max = 21;
      } else if (py04OBmax == 60) {
        tonB1Max = 12;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB1Max = 3;
      }
    } else if (bodenartOB1 == 414) {
      // sT
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB1Min = 30;
      } else if (py04OBmin == 15) {
        tonB1Min = 25;
      } else if (py04OBmin == 30) {
        tonB1Min = 21;
      } else if (py04OBmin == 60) {
        tonB1Min = 12;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB1Min = 3;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB1Max = 30;
      } else if (py04OBmax == 15) {
        tonB1Max = 25;
      } else if (py04OBmax == 30) {
        tonB1Max = 21;
      } else if (py04OBmax == 60) {
        tonB1Max = 12;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB1Max = 3;
      }
    } else if (bodenartOB1 == 534) {
      // lT
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB1Min = 45;
      } else if (py04OBmin == 15) {
        tonB1Min = 38;
      } else if (py04OBmin == 30) {
        tonB1Min = 32;
      } else if (py04OBmin == 60) {
        tonB1Min = 18;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB1Min = 4;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB1Max = 45;
      } else if (py04OBmax == 15) {
        tonB1Max = 38;
      } else if (py04OBmax == 30) {
        tonB1Max = 32;
      } else if (py04OBmax == 60) {
        tonB1Max = 18;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB1Max = 4;
      }
    } else if (bodenartOB1 == 504) {
      // T
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB1Min = 70;
      } else if (py04OBmin == 15) {
        tonB1Min = 60;
      } else if (py04OBmin == 30) {
        tonB1Min = 50;
      } else if (py04OBmin == 60) {
        tonB1Min = 30;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB1Min = 5;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB1Max = 70;
      } else if (py04OBmax == 15) {
        tonB1Max = 60;
      } else if (py04OBmax == 30) {
        tonB1Max = 50;
      } else if (py04OBmax == 60) {
        tonB1Max = 30;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB1Max = 5;
      }
    }

    // Bodenart 2
    if (bodenartOB2 == 101) {
      // S
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB2Min = 2.5;
      } else if (py04OBmin == 15) {
        tonB2Min = 2;
      } else if (py04OBmin == 30) {
        tonB2Min = 2;
      } else if (py04OBmin == 60) {
        tonB2Min = 1;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB2Min = 0;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB2Max = 2.5;
      } else if (py04OBmax == 15) {
        tonB2Max = 2;
      } else if (py04OBmax == 30) {
        tonB2Max = 2;
      } else if (py04OBmax == 60) {
        tonB2Max = 1;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB2Max = 0;
      }
    } else if (bodenartOB2 == 121) {
      // uS
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB2Min = 2.5;
      } else if (py04OBmin == 15) {
        tonB2Min = 2;
      } else if (py04OBmin == 30) {
        tonB2Min = 2;
      } else if (py04OBmin == 60) {
        tonB2Min = 1;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB2Min = 0;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB2Max = 2.5;
      } else if (py04OBmax == 15) {
        tonB2Max = 2;
      } else if (py04OBmax == 30) {
        tonB2Max = 2;
      } else if (py04OBmax == 60) {
        tonB2Max = 1;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB2Max = 0;
      }
    } else if (bodenartOB2 == 212) {
      // sU
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB2Min = 7.5;
      } else if (py04OBmin == 15) {
        tonB2Min = 6;
      } else if (py04OBmin == 30) {
        tonB2Min = 5;
      } else if (py04OBmin == 60) {
        tonB2Min = 3;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB2Min = 1;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB2Max = 7.5;
      } else if (py04OBmax == 15) {
        tonB2Max = 6;
      } else if (py04OBmax == 30) {
        tonB2Max = 5;
      } else if (py04OBmax == 60) {
        tonB2Max = 3;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB2Max = 1;
      }
    } else if (bodenartOB2 == 202) {
      // U
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB2Min = 10;
      } else if (py04OBmin == 15) {
        tonB2Min = 9;
      } else if (py04OBmin == 30) {
        tonB2Min = 7;
      } else if (py04OBmin == 60) {
        tonB2Min = 4;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB2Min = 1;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB2Max = 10;
      } else if (py04OBmax == 15) {
        tonB2Max = 9;
      } else if (py04OBmax == 30) {
        tonB2Max = 7;
      } else if (py04OBmax == 60) {
        tonB2Max = 4;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB2Max = 1;
      }
    } else if (bodenartOB2 == 231) {
      // lS
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB2Min = 10;
      } else if (py04OBmin == 15) {
        tonB2Min = 9;
      } else if (py04OBmin == 30) {
        tonB2Min = 7;
      } else if (py04OBmin == 60) {
        tonB2Min = 4;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB2Min = 1;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB2Max = 10;
      } else if (py04OBmax == 15) {
        tonB2Max = 9;
      } else if (py04OBmax == 30) {
        tonB2Max = 7;
      } else if (py04OBmax == 60) {
        tonB2Max = 4;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB2Max = 1;
      }
    } else if (bodenartOB2 == 332) {
      // lU
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB2Min = 20;
      } else if (py04OBmin == 15) {
        tonB2Min = 17;
      } else if (py04OBmin == 30) {
        tonB2Min = 14;
      } else if (py04OBmin == 60) {
        tonB2Min = 8;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB2Min = 2;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB2Max = 20;
      } else if (py04OBmax == 15) {
        tonB2Max = 17;
      } else if (py04OBmax == 30) {
        tonB2Max = 14;
      } else if (py04OBmax == 60) {
        tonB2Max = 8;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB2Max = 2;
      }
    } else if (bodenartOB2 == 342) {
      // tS
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB2Min = 20;
      } else if (py04OBmin == 15) {
        tonB2Min = 17;
      } else if (py04OBmin == 30) {
        tonB2Min = 14;
      } else if (py04OBmin == 60) {
        tonB2Min = 8;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB2Min = 2;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB2Max = 20;
      } else if (py04OBmax == 15) {
        tonB2Max = 17;
      } else if (py04OBmax == 30) {
        tonB2Max = 14;
      } else if (py04OBmax == 60) {
        tonB2Max = 8;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB2Max = 2;
      }
    } else if (bodenartOB2 == 313) {
      // sL
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB2Min = 20;
      } else if (py04OBmin == 15) {
        tonB2Min = 17;
      } else if (py04OBmin == 30) {
        tonB2Min = 14;
      } else if (py04OBmin == 60) {
        tonB2Min = 8;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB2Min = 2;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB2Max = 20;
      } else if (py04OBmax == 15) {
        tonB2Max = 17;
      } else if (py04OBmax == 30) {
        tonB2Max = 14;
      } else if (py04OBmax == 60) {
        tonB2Max = 8;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB2Max = 2;
      }
    } else if (bodenartOB2 == 423) {
      // uL
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB2Min = 30;
      } else if (py04OBmin == 15) {
        tonB2Min = 25;
      } else if (py04OBmin == 30) {
        tonB2Min = 21;
      } else if (py04OBmin == 60) {
        tonB2Min = 12;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB2Min = 3;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB2Max = 30;
      } else if (py04OBmax == 15) {
        tonB2Max = 25;
      } else if (py04OBmax == 30) {
        tonB2Max = 21;
      } else if (py04OBmax == 60) {
        tonB2Max = 12;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB2Max = 3;
      }
    } else if (bodenartOB2 == 403) {
      // L
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB2Min = 30;
      } else if (py04OBmin == 15) {
        tonB2Min = 25;
      } else if (py04OBmin == 30) {
        tonB2Min = 21;
      } else if (py04OBmin == 60) {
        tonB2Min = 12;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB2Min = 3;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB2Max = 30;
      } else if (py04OBmax == 15) {
        tonB2Max = 25;
      } else if (py04OBmax == 30) {
        tonB2Max = 21;
      } else if (py04OBmax == 60) {
        tonB2Max = 12;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB2Max = 3;
      }
    } else if (bodenartOB2 == 414) {
      // sT
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB2Min = 30;
      } else if (py04OBmin == 15) {
        tonB2Min = 25;
      } else if (py04OBmin == 30) {
        tonB2Min = 21;
      } else if (py04OBmin == 60) {
        tonB2Min = 12;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB2Min = 3;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB2Max = 30;
      } else if (py04OBmax == 15) {
        tonB2Max = 25;
      } else if (py04OBmax == 30) {
        tonB2Max = 21;
      } else if (py04OBmax == 60) {
        tonB2Max = 12;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB2Max = 3;
      }
    } else if (bodenartOB2 == 534) {
      // lT
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB2Min = 45;
      } else if (py04OBmin == 15) {
        tonB2Min = 38;
      } else if (py04OBmin == 30) {
        tonB2Min = 32;
      } else if (py04OBmin == 60) {
        tonB2Min = 18;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB2Min = 4;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB2Max = 45;
      } else if (py04OBmax == 15) {
        tonB2Max = 38;
      } else if (py04OBmax == 30) {
        tonB2Max = 32;
      } else if (py04OBmax == 60) {
        tonB2Max = 18;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB2Max = 4;
      }
    } else if (bodenartOB2 == 504) {
      // T
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        tonB2Min = 70;
      } else if (py04OBmin == 15) {
        tonB2Min = 60;
      } else if (py04OBmin == 30) {
        tonB2Min = 50;
      } else if (py04OBmin == 60) {
        tonB2Min = 30;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        tonB2Min = 5;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        tonB2Max = 70;
      } else if (py04OBmax == 15) {
        tonB2Max = 60;
      } else if (py04OBmax == 30) {
        tonB2Max = 50;
      } else if (py04OBmax == 60) {
        tonB2Max = 30;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        tonB2Max = 5;
      }
    }

    double[] tonArr = new double[]{tonB1Max, tonB1Min, tonB2Max, tonB2Min};
    double sum = 0;
    int count = 0;

    for (double val : tonArr) {
      if (val > 0) {
        sum += val;
        ++count;
      }
    }

    if (count == 0) {
      this.resultMap.put("Transform", "FEHLER : Modifikation nach Tongehalt konnte nicht durchgeführt werden");
      return;
    }

    double tonNeu = sum / count;

    // 3. Schritt: Modifikation des Humusgehalts in Abhängigkeit vom Skelettgehalt

    // Vier Werte humus min, humus max, Skelett max, Skelett min
    double humusMinMin = -1;
    double humusMinMax = -1;
    double humusMaxMin = -1;
    double humusMaxMax = -1;


    // Humus MIN
    if (py03min == 0) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        humusMinMin = 0;
      } else if (py04OBmin == 15) {
        humusMinMin = 0;
      } else if (py04OBmin == 30) {
        humusMinMin = 0;
      } else if (py04OBmin == 60) {
        humusMinMin = 0;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        humusMinMin = 0;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        humusMinMax = 0;
      } else if (py04OBmax == 15) {
        humusMinMax = 0;
      } else if (py04OBmax == 30) {
        humusMinMax = 0;
      } else if (py04OBmax == 60) {
        humusMinMax = 0;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        humusMinMax = 0;
      }
    } else if (py03min == 1) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        humusMinMin = 1;
      } else if (py04OBmin == 15) {
        humusMinMin = 1;
      } else if (py04OBmin == 30) {
        humusMinMin = 1;
      } else if (py04OBmin == 60) {
        humusMinMin = 1;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        humusMinMin = 0;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        humusMinMax = 1;
      } else if (py04OBmax == 15) {
        humusMinMax = 1;
      } else if (py04OBmax == 30) {
        humusMinMax = 1;
      } else if (py04OBmax == 60) {
        humusMinMax = 1;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        humusMinMax = 0;
      }
    } else if (py03min == 2) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        humusMinMin = 2;
      } else if (py04OBmin == 15) {
        humusMinMin = 2;
      } else if (py04OBmin == 30) {
        humusMinMin = 2;
      } else if (py04OBmin == 60) {
        humusMinMin = 1;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        humusMinMin = 0;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        humusMinMax = 2;
      } else if (py04OBmax == 15) {
        humusMinMax = 2;
      } else if (py04OBmax == 30) {
        humusMinMax = 2;
      } else if (py04OBmax == 60) {
        humusMinMax = 1;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        humusMinMax = 0;
      }
    } else if (py03min == 3) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        humusMinMin = 3;
      } else if (py04OBmin == 15) {
        humusMinMin = 3;
      } else if (py04OBmin == 30) {
        humusMinMin = 3;
      } else if (py04OBmin == 60) {
        humusMinMin = 2;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        humusMinMin = 0;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        humusMinMax = 3;
      } else if (py04OBmax == 15) {
        humusMinMax = 3;
      } else if (py04OBmax == 30) {
        humusMinMax = 3;
      } else if (py04OBmax == 60) {
        humusMinMax = 2;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        humusMinMax = 0;
      }
    } else if (py03min == 4) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        humusMinMin = 4;
      } else if (py04OBmin == 15) {
        humusMinMin = 4;
      } else if (py04OBmin == 30) {
        humusMinMin = 4;
      } else if (py04OBmin == 60) {
        humusMinMin = 3;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        humusMinMin = 0;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        humusMinMax = 4;
      } else if (py04OBmax == 15) {
        humusMinMax = 4;
      } else if (py04OBmax == 30) {
        humusMinMax = 3;
      } else if (py04OBmax == 60) {
        humusMinMax = 3;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        humusMinMax = 0;
      }
    } else if (py03min == 5) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        humusMinMin = 5;
      } else if (py04OBmin == 15) {
        // Nothing
      } else if (py04OBmin == 30) {
        // Nothing
      } else if (py04OBmin == 60) {
        // Nothing
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        // Nothing
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        humusMinMax = 5;
      } else if (py04OBmax == 15) {
        // Nothing
      } else if (py04OBmax == 30) {
        // Nothing
      } else if (py04OBmax == 60) {
        // Nothing
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        // Nothing
      }
    }

    // Humus MAX
    if (py03max == 0) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        humusMaxMin = 0;
      } else if (py04OBmin == 15) {
        humusMaxMin = 0;
      } else if (py04OBmin == 30) {
        humusMaxMin = 0;
      } else if (py04OBmin == 60) {
        humusMaxMin = 0;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        humusMaxMin = 0;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        humusMaxMax = 0;
      } else if (py04OBmax == 15) {
        humusMaxMax = 0;
      } else if (py04OBmax == 30) {
        humusMaxMax = 0;
      } else if (py04OBmax == 60) {
        humusMaxMax = 0;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        humusMaxMax = 0;
      }
    } else if (py03max == 1) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        humusMaxMin = 1;
      } else if (py04OBmin == 15) {
        humusMaxMin = 1;
      } else if (py04OBmin == 30) {
        humusMaxMin = 1;
      } else if (py04OBmin == 60) {
        humusMaxMin = 1;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        humusMaxMin = 0;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        humusMaxMax = 1;
      } else if (py04OBmax == 15) {
        humusMaxMax = 1;
      } else if (py04OBmax == 30) {
        humusMaxMax = 1;
      } else if (py04OBmax == 60) {
        humusMaxMax = 1;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        humusMaxMax = 0;
      }
    } else if (py03max == 2) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        humusMaxMin = 2;
      } else if (py04OBmin == 15) {
        humusMaxMin = 2;
      } else if (py04OBmin == 30) {
        humusMaxMin = 2;
      } else if (py04OBmin == 60) {
        humusMaxMin = 1;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        humusMaxMin = 0;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        humusMaxMax = 2;
      } else if (py04OBmax == 15) {
        humusMaxMax = 2;
      } else if (py04OBmax == 30) {
        humusMaxMax = 2;
      } else if (py04OBmax == 60) {
        humusMaxMax = 1;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        humusMaxMax = 0;
      }
    } else if (py03max == 3) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        humusMaxMin = 3;
      } else if (py04OBmin == 15) {
        humusMaxMin = 3;
      } else if (py04OBmin == 30) {
        humusMaxMin = 3;
      } else if (py04OBmin == 60) {
        humusMaxMin = 2;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        humusMaxMin = 0;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        humusMaxMax = 3;
      } else if (py04OBmax == 15) {
        humusMaxMax = 3;
      } else if (py04OBmax == 30) {
        humusMaxMax = 3;
      } else if (py04OBmax == 60) {
        humusMaxMax = 2;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        humusMaxMax = 0;
      }
    } else if (py03max == 4) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        humusMaxMin = 4;
      } else if (py04OBmin == 15) {
        humusMaxMin = 4;
      } else if (py04OBmin == 30) {
        humusMaxMin = 4;
      } else if (py04OBmin == 60) {
        humusMaxMin = 3;
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        humusMaxMin = 0;
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        humusMaxMax = 4;
      } else if (py04OBmax == 15) {
        humusMaxMax = 4;
      } else if (py04OBmax == 30) {
        humusMaxMax = 3;
      } else if (py04OBmax == 60) {
        humusMaxMax = 3;
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        humusMaxMax = 0;
      }
    } else if (py03max == 5) {
      // result with SkelettMin
      if (py04OBmin == 0 || py04OBmin == 5) {
        humusMaxMin = 5;
      } else if (py04OBmin == 15) {
        // Nothing
      } else if (py04OBmin == 30) {
        // Nothing
      } else if (py04OBmin == 60) {
        // Nothing
      } else if (py04OBmin == 90 || py04OBmin == 100) {
        // Nothing
      }
      // result with SkelettMax
      if (py04OBmax == 0 || py04OBmax == 5) {
        humusMaxMax = 5;
      } else if (py04OBmax == 15) {
        // Nothing
      } else if (py04OBmax == 30) {
        // Nothing
      } else if (py04OBmax == 60) {
        // Nothing
      } else if (py04OBmax == 90 || py04OBmax == 100) {
        // Nothing
      }
    }

    double[] humusArr = new double[]{humusMinMin, humusMinMax, humusMaxMin, humusMaxMax};
    sum = 0;
    count = 0;

    for (double val : humusArr) {
      if (val > 0) {
        sum += val;
        ++count;
      }
    }

    if (count == 0) {
      this.resultMap.put("Transform", "FEHLER : Modifikation des Humusgehaltes konnte nicht durchgeführt werden");
      return;
    }

    int humusNeu = Long.valueOf(Math.round(sum / count)).intValue();

    // 4. Schritt: Gesamtbewertung Transform unter Berücksichtigung von mikrobieller Abbauleistung, Humus- und Tongehalt

    if (humusNeu == 0 || humusNeu == 1) {
      if (tonNeu < 10) {
        if (level == 1) {
          result = 5;
        } else if (level == 2) {
          result = 5;
        } else if (level == 3) {
          result = 5;
        }
      } else if (tonNeu >= 10 && tonNeu <= 30) {
        if (level == 1) {
          result = 5;
        } else if (level == 2) {
          result = 4;
        } else if (level == 3) {
          result = 3;
        }
      } else if (tonNeu > 30 && tonNeu <= 45) {
        if (level == 1) {
          result = 5;
        } else if (level == 2) {
          result = 3;
        } else if (level == 3) {
          result = 3;
        }
      } else if (tonNeu > 45) {
        if (level == 1) {
          result = 4;
        } else if (level == 2) {
          result = 3;
        } else if (level == 3) {
          result = 2;
        }
      }
    } else if (humusNeu == 2) {
      if (tonNeu < 10) {
        if (level == 1) {
          result = 5;
        } else if (level == 2) {
          result = 5;
        } else if (level == 3) {
          result = 4;
        }
      } else if (tonNeu >= 10 && tonNeu <= 30) {
        if (level == 1) {
          result = 4;
        } else if (level == 2) {
          result = 3;
        } else if (level == 3) {
          result = 3;
        }
      } else if (tonNeu > 30 && tonNeu <= 45) {
        if (level == 1) {
          result = 3;
        } else if (level == 2) {
          result = 3;
        } else if (level == 3) {
          result = 3;
        }
      } else if (tonNeu > 45) {
        if (level == 1) {
          result = 3;
        } else if (level == 2) {
          result = 2;
        } else if (level == 3) {
          result = 1;
        }
      }
    } else if (humusNeu == 3) {
      if (tonNeu < 10) {
        if (level == 1) {
          result = 5;
        } else if (level == 2) {
          result = 4;
        } else if (level == 3) {
          result = 3;
        }
      } else if (tonNeu >= 10 && tonNeu <= 30) {
        if (level == 1) {
          result = 3;
        } else if (level == 2) {
          result = 3;
        } else if (level == 3) {
          result = 2;
        }
      } else if (tonNeu > 30 && tonNeu <= 45) {
        if (level == 1) {
          result = 3;
        } else if (level == 2) {
          result = 2;
        } else if (level == 3) {
          result = 1;
        }
      } else if (tonNeu > 45) {
        if (level == 1) {
          result = 2;
        } else if (level == 2) {
          result = 1;
        } else if (level == 3) {
          result = 1;
        }
      }
    } else if (humusNeu == 4 || humusNeu == 5) {
      if (tonNeu < 10) {
        if (level == 1) {
          result = 4;
        } else if (level == 2) {
          result = 3;
        } else if (level == 3) {
          result = 2;
        }
      } else if (tonNeu >= 10 && tonNeu <= 30) {
        if (level == 1) {
          result = 2;
        } else if (level == 2) {
          result = 2;
        } else if (level == 3) {
          result = 1;
        }
      } else if (tonNeu > 30 && tonNeu <= 45) {
        if (level == 1) {
          result = 2;
        } else if (level == 2) {
          result = 1;
        } else if (level == 3) {
          result = 1;
        }
      } else if (tonNeu > 45) {
        if (level == 1) {
          result = 1;
        } else if (level == 2) {
          result = 1;
        } else if (level == 3) {
          result = 1;
        }
      }
    }

    if (result != null) {
      // save to horizont
      this.bk.setAttributeValue("Transform", String.valueOf(result));
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': 1D.2 saved with value '" + result + "'");
      this.resultMap.put("Transform", String.valueOf(result));
    } else {
      this.resultMap.put("Transform", "FEHLER : Konnte nicht ermittelt werden");
    }
  }

  public void calculate1D4() throws Exception {
    Integer result = null;

    // get attributes
    long bodenartOB1 = ((KeyAttribute) this.bk.getAttribute("Bodenart_OB_1")).getId();
    long bodenartOB2 = ((KeyAttribute) this.bk.getAttribute("Bodenart_OB_2")).getId();
    long bodenartUB1 = ((KeyAttribute) this.bk.getAttribute("Bodenart_UB_1")).getId();
    long bodenartUB2 = ((KeyAttribute) this.bk.getAttribute("Bodenart_UB_2")).getId();
    float py01 = this.bk.getAttributeValueFloat("Py01_OB_cm");
    float py02 = this.bk.getAttributeValueFloat("Py02_UB_cm");
    long py03min = ((KeyAttribute) this.bk.getAttribute("Py03_Hu_min")).getId();
    long py03max = ((KeyAttribute) this.bk.getAttribute("Py03_Hu_max")).getId();
    long py04OBmin = ((KeyAttribute) this.bk.getAttribute("Py04_OB_Sk_min")).getId();
    long py04OBmax = ((KeyAttribute) this.bk.getAttribute("Py04_OB_Sk_max")).getId();
    long py04OB = -1;
    long py04UBmin = ((KeyAttribute) this.bk.getAttribute("Py04_UB_Sk_min")).getId();
    long py04UBmax = ((KeyAttribute) this.bk.getAttribute("Py04_UB_Sk_max")).getId();
    long py04UB = -1;
    
    
    float precip = ((SeppProject)this.bk.getApplication()).getProjectElement().getAttributeValueFloat("precip_year");
    float evaporation = ((SeppProject)this.bk.getApplication()).getProjectElement().getAttributeValueFloat("evaporation");

    if (bodenartOB1 < 0 && bodenartOB2 < 0) {
      this.resultMap.put("FiltPuff_Nit", "FEHLER : Bodenart des Oberbodens nicht vorhanden");
      return;
    }

    if (precip < 0) {
      this.resultMap.put("FiltPuff_Nit", "FEHLER : Es fehlt der Jahresniederschlag");
      return;
    }

    if (evaporation < 0) {
      this.resultMap.put("FiltPuff_Nit", "FEHLER : Es fehlt die mittl. jährliche Verdunstung");
      return;
    }

    if (py01 < 0) {
      this.resultMap.put("FiltPuff_Nit", "FEHLER : Mächtigkeit des Oberbodens nicht vorhanden");
      return;
    }

    // Skelet Oberboden
    if (py04OBmin >= 0 && py04OBmax >= 0) {
      py04OB = (py04OBmin + py04OBmax) / 2;
    } else if (py04OBmin >= 0 && py04OBmax < 0) {
      py04OB = py04OBmin;
    } else if (py04OBmin < 0 && py04OBmax >= 0) {
      py04OB = py04OBmax;
    }

    if (py04OB < 0) {
      this.resultMap.put("FiltPuff_Nit", "FEHLER : Skelettgehalt für Oberboden nicht vorhanden");
      return;
    }

    // Skelet Unterboden
    if (py04UBmin >= 0 && py04UBmax >= 0) {
      py04UB = (py04UBmin + py04UBmax) / 2;
    } else if (py04UBmin >= 0 && py04UBmax < 0) {
      py04UB = py04UBmin;
    } else if (py04UBmin < 0 && py04UBmax >= 0) {
      py04UB = py04UBmax;
    }

    if (py04UB < 0) {
      this.resultMap.put("FiltPuff_Nit", "FEHLER : Skelettgehalt für Unterboden nicht vorhanden");
      return;
    }

    // 1. Schritt: Bestimmung der Menge des jährlichen Sickerwassers
    double ofa = -1;

    if (bodenartOB1 == 504 || bodenartOB1 == 534 || bodenartOB1 == 414) {
      ofa = 8;
    } else if (bodenartOB1 == 403 || bodenartOB1 == 202 || bodenartOB1 == 423
            || bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 212
            || bodenartOB1 == 341) {
      ofa = 4.5;
    } else if (bodenartOB1 == 101 || bodenartOB1 == 121 || bodenartOB1 == 231) {
      ofa = 1.5;
    }

    if (ofa < 0) {
      this.resultMap.put("FiltPuff_Nit", "FEHLER : Anteil des Oberflächenanflusses am Gesamtabfluss konnte nicht ermittelt werden");
      return;
    }

    // Sickerwasser
    double sw = (precip - evaporation) * (1 - ofa / 100);

    // 2. Schritt: Bestimmung der Feldkapazität über Pedotransfertabellen auf Basis elementarer Parameter

    // 2a) Abschätzung des relevanten Porenvolumens auf Basis der Bodenart

    float fkOB1 = -1;
    if (bodenartOB1 == 504) {
      fkOB1 = 49;
    } else if (bodenartOB1 == 534) {
      fkOB1 = 48;
    } else if (bodenartOB1 == 202) {
      fkOB1 = 35.5f;
    } else if (bodenartOB1 == 403) {
      fkOB1 = 39.5f;
    } else if (bodenartOB1 == 423) {
      fkOB1 = 38.5f;
    } else if (bodenartOB1 == 414) {
      fkOB1 = 36;
    } else if (bodenartOB1 == 313) {
      fkOB1 = 34;
    } else if (bodenartOB1 == 332) {
      fkOB1 = 36;
    } else if (bodenartOB1 == 341) {
      fkOB1 = 26.5f;
    } else if (bodenartOB1 == 231) {
      fkOB1 = 30.5f;
    } else if (bodenartOB1 == 212) {
      fkOB1 = 33.5f;
    } else if (bodenartOB1 == 121) {
      fkOB1 = 30;
    } else if (bodenartOB1 == 101) {
      fkOB1 = 20;
    }

    float fkOB2 = -1;
    if (bodenartOB2 == 504) {
      fkOB2 = 49;
    } else if (bodenartOB2 == 534) {
      fkOB2 = 48;
    } else if (bodenartOB2 == 202) {
      fkOB2 = 35.5f;
    } else if (bodenartOB2 == 403) {
      fkOB2 = 39.5f;
    } else if (bodenartOB2 == 423) {
      fkOB2 = 38.5f;
    } else if (bodenartOB2 == 414) {
      fkOB2 = 36;
    } else if (bodenartOB2 == 313) {
      fkOB2 = 34;
    } else if (bodenartOB2 == 332) {
      fkOB2 = 36;
    } else if (bodenartOB2 == 341) {
      fkOB2 = 26.5f;
    } else if (bodenartOB2 == 231) {
      fkOB2 = 30.5f;
    } else if (bodenartOB2 == 212) {
      fkOB2 = 33.5f;
    } else if (bodenartOB2 == 121) {
      fkOB2 = 30;
    } else if (bodenartOB2 == 101) {
      fkOB2 = 20;
    }

    float fkUB1 = -1;
    if (bodenartUB1 == 504) {
      fkUB1 = 49;
    } else if (bodenartUB1 == 534) {
      fkUB1 = 48;
    } else if (bodenartUB1 == 202) {
      fkUB1 = 35.5f;
    } else if (bodenartUB1 == 403) {
      fkUB1 = 39.5f;
    } else if (bodenartUB1 == 423) {
      fkUB1 = 38.5f;
    } else if (bodenartUB1 == 414) {
      fkUB1 = 36;
    } else if (bodenartUB1 == 313) {
      fkUB1 = 34;
    } else if (bodenartUB1 == 332) {
      fkUB1 = 36;
    } else if (bodenartUB1 == 341) {
      fkUB1 = 26.5f;
    } else if (bodenartUB1 == 231) {
      fkUB1 = 30.5f;
    } else if (bodenartUB1 == 212) {
      fkUB1 = 33.5f;
    } else if (bodenartUB1 == 121) {
      fkUB1 = 30;
    } else if (bodenartUB1 == 101) {
      fkUB1 = 20;
    }
    float fkUB2 = -1;
    if (bodenartUB2 == 504) {
      fkUB2 = 49;
    } else if (bodenartUB2 == 534) {
      fkUB2 = 48;
    } else if (bodenartUB2 == 202) {
      fkUB2 = 35.5f;
    } else if (bodenartUB2 == 403) {
      fkUB2 = 39.5f;
    } else if (bodenartUB2 == 423) {
      fkUB2 = 38.5f;
    } else if (bodenartUB2 == 414) {
      fkUB2 = 36;
    } else if (bodenartUB2 == 313) {
      fkUB2 = 34;
    } else if (bodenartUB2 == 332) {
      fkUB2 = 36;
    } else if (bodenartUB2 == 341) {
      fkUB2 = 26.5f;
    } else if (bodenartUB2 == 231) {
      fkUB2 = 30.5f;
    } else if (bodenartUB2 == 212) {
      fkUB2 = 33.5f;
    } else if (bodenartUB2 == 121) {
      fkUB2 = 30;
    } else if (bodenartUB2 == 101) {
      fkUB2 = 20;
    }


    // Feldkapazität
    double fkOben = -1;
    if (fkOB1 >= 0 && fkOB2 >= 0) {
      fkOben = (fkOB1 + fkOB2) / 2;
    } else if (fkOB1 >= 0 && fkOB2 < 0) {
      fkOben = fkOB1;
    } else if (fkOB1 < 0 && fkOB2 >= 0) {
      fkOben = fkOB2;
    }
    if (fkOben < 0) {
      this.resultMap.put("FiltPuff_Nit", "FEHLER : FK konnte für den Unterboden nicht berechnet werden");
      return;
    }

    double fkUnten = -1;
    if (fkUB1 >= 0 && fkUB2 >= 0) {
      fkUnten = (fkUB1 + fkUB2) / 2;
    } else if (fkUB1 >= 0 && fkUB2 < 0) {
      fkUnten = fkUB1;
    } else if (fkUB1 < 0 && fkUB2 >= 0) {
      fkUnten = fkUB2;
    }
    if (fkUnten < 0) {
      this.resultMap.put("FiltPuff_Nit", "FEHLER : FK konnte für den Unterboden nicht berechnet werden");
      return;
    }

    // 2b) Korrektur dieses Wertes nach dem Humusgehalt (Py03)
    // Humus MIN
    double humusMin = -1;
    double fkMin = -1;
    if (py03min == 0) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = 0;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 0;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 0;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 0;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 0;
      }
    } else if (py03min == 1) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = 1.5;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 1.5;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 1.5;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 2.5;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 3;
      }
    } else if (py03min == 2) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = 3.5;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 3.5;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 3.5;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 4;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 6;
      }
    } else if (py03min == 3) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMin = 7.5;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMin = 8;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMin = 9;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMin = 10;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMin = 11.5;
      }
    } else if (py03min == 4) {
      fkMin = 62;
    } else if (py03min == 5) {
      fkMin = 76;
    }

    // Humus MAX
    double humusMax = -1;
    double fkMax = -1;
    if (py03max == 0) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = 0;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 0;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 0;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 0;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 0;
      }
    } else if (py03max == 1) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = 1.5;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 1.5;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 1.5;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 2.5;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 3;
      }
    } else if (py03max == 2) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = 3.5;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 3.5;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 3.5;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 4;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 6;
      }
    } else if (py03max == 3) {
      if (bodenartOB1 == 101 || bodenartOB1 == 121) {
        humusMax = 7.5;
      } else if (bodenartOB1 == 231 || bodenartOB1 == 341) {
        humusMax = 8;
      } else if (bodenartOB1 == 212 || bodenartOB1 == 202) {
        humusMax = 9;
      } else if (bodenartOB1 == 332 || bodenartOB1 == 313 || bodenartOB1 == 423 || bodenartOB1 == 403) {
        humusMax = 10;
      } else if (bodenartOB1 == 414 || bodenartOB1 == 534 || bodenartOB1 == 504) {
        humusMax = 11.5;
      }
    } else if (py03max == 4) {
      fkMax = 62;
    } else if (py03max == 5) {
      fkMax = 76;
    }

    // Korrektur Humus
    if (humusMin >= 0 && humusMax >= 0) {
      // Mittelwert beider Korrekturwerte, da sie beide gesetzt wurden
      double humusCorr = (humusMin + humusMax) / 2;
      fkOben += humusCorr;
    } else if (humusMin >= 0 && humusMax < 0) {
      // Mittelwert aus Korrektur und Ausnahme (fkMax), falls gesetzt
      double fkObenMod = fkOben + humusMin;
      if (fkMax > 0) {
        fkOben = (fkObenMod + fkMax) / 2;
      } else {
        fkOben += humusMin;
      }
    } else if (humusMin < 0 && humusMax >= 0) {
      // Mittelwert aus Korrektur und Ausnahme (fkMin), falls gesetzt
      double fkObenMod = fkOben + humusMax;
      if (fkMin > 0) {
        fkOben = (fkObenMod + fkMin) / 2;
      } else {
        fkOben += humusMax;
      }
    } else if (humusMin < 0 && humusMax < 0 && fkMin > 0 && fkMax > 0) {
      fkOben = (fkMin + fkMax) / 2;
    }

    // 2c) Berechnung der Feldkapazität in [l/m²]

    double fksum = -1;
    if (py01 >= 0 && py04OB >= 0 && fkOben >= 0 && py02 >= 0 && py04UB >= 0 && fkUnten >= 0) {
      fksum = (py01 * 10 * (1 - py04OB / 100)) * (fkOben / 100)
              + (py02 * 10 * (1 - py04UB / 100)) * (fkUnten / 100);
    }

    if (fksum <= 0) {
      this.resultMap.put("FiltPuff_Nit", "FEHLER : FK konnte nicht berechnet werden [Ungültiger Wert]");
      return;
    }

    // 3. Schritt: Berechnung und Bewertung der jährlichen Austauschhäufigkeit des Bodenwassers

    double swAus = sw / fksum;

    // BEWERTUNG
    if (swAus >= 2.5) {
      result = 5;
    } else if (swAus >= 1.5 && swAus < 2.5) {
      result = 4;
    } else if (swAus >= 1.0 && swAus < 1.5) {
      result = 3;
    } else if (swAus >= 0.7 && swAus < 1.0) {
      result = 2;
    } else if (swAus < 0.7) {
      result = 1;
    }

    if (result != null) {
      this.bk.setAttributeValue("FiltPuff_Nit", String.valueOf(result));
      Util.getLogger(this.getClass().getName()).log(Level.INFO, "Bewertungseinheit '" + this.bk.getId() + "': 1D.4 saved with value '" + result + "'");
      this.resultMap.put("FiltPuff_Nit", String.valueOf(result));
    } else {
      this.resultMap.put("FiltPuff_Nit", "FEHLER : Konnte nicht berechnet werden");
    }
  }
}
