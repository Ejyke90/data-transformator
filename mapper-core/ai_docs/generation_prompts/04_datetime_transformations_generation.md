# Date/Time Transformation Generation Prompt (Medium Confidence)

**Task**: Generate date/time conversion helpers with timezone handling
**Category**: Phase 3 - Format conversion and timestamp handling
**Reference**: 03_datetime_transformations_medium_confidence.md

---

## Context for Date/Time Transformations

Generate @Named helper methods that convert ISO 20022 date/time formats to timestamp-micros (Long). Handle timezone conversion, choice structures, and validation.

---

## Core DateTime Conversion Helper

```java
@Mapping(target = "payment_creation_date", source = "grpHdr.creDtTm", qualifiedByName = "convertToTimestampMicros")
@Mapping(target = "Pods_last_updated_datetime", source = "grpHdr.creDtTm", qualifiedByName = "convertToTimestampMicros")

@Named("convertToTimestampMicros")
Long convertToTimestampMicros(XMLGregorianCalendar dateTime) {
    if (dateTime == null) return null;
    
    try {
        // Convert to GregorianCalendar, then to milliseconds, then to microseconds
        return dateTime.toGregorianCalendar().getTimeInMillis() * 1000L;
    } catch (Exception e) {
        log.error("Error converting datetime to timestamp: {}", dateTime, e);
        throw new PaymentMappingException("Invalid datetime format: " + dateTime);
    }
}
```

---

## DateAndDateTime2Choice Conversion

```java
@Mapping(target = "payment_completion_date", source = "pmtInf.reqdExctnDt", qualifiedByName = "convertDateTimeChoiceToTimestampMicros")

@Named("convertDateTimeChoiceToTimestampMicros")
Long convertDateTimeChoiceToTimestampMicros(DateAndDateTime2Choice choice) {
    if (choice == null) return null;
    
    if (choice.getDtTm() != null) {
        // DateTime present - convert directly
        return convertToTimestampMicros(choice.getDtTm());
    } else if (choice.getDt() != null) {
        // Date only - convert to midnight UTC
        try {
            XMLGregorianCalendar cal = (XMLGregorianCalendar) choice.getDt().clone();
            cal.setTime(0, 0, 0, 0); // Set to midnight
            return convertToTimestampMicros(cal);
        } catch (Exception e) {
            log.error("Error converting date choice to timestamp: {}", choice.getDt(), e);
            throw new PaymentMappingException("Invalid date format in choice: " + choice.getDt());
        }
    }
    return null;
}
```

---

## Date-Only Conversion

```java
@Mapping(target = "pooling_adjustment_date", source = "pmtInf.poolAdjstmntDt", qualifiedByName = "convertDateToTimestampMicros")
@Mapping(target = "transaction_date", source = "cdtTrfTxInf.pmtId.txDt", qualifiedByName = "convertDateToTimestampMicros")

@Named("convertDateToTimestampMicros")
Long convertDateToTimestampMicros(XMLGregorianCalendar date) {
    if (date == null) return null;
    
    try {
        // Clone to avoid modifying original
        XMLGregorianCalendar cal = (XMLGregorianCalendar) date.clone();
        cal.setTime(0, 0, 0, 0); // Set to midnight UTC
        return convertToTimestampMicros(cal);
    } catch (Exception e) {
        log.error("Error converting date to timestamp: {}", date, e);
        throw new PaymentMappingException("Invalid date format: " + date);
    }
}
```

---

## UTC Conversion with Timezone Handling

```java
@Named("convertToTimestampMicrosUTC")
Long convertToTimestampMicrosUTC(XMLGregorianCalendar dateTime) {
    if (dateTime == null) return null;
    
    try {
        // Convert to UTC if timezone is specified
        GregorianCalendar cal = dateTime.toGregorianCalendar();
        
        // If no timezone specified, assume UTC
        if (dateTime.getTimezone() == DatatypeConstants.FIELD_UNDEFINED) {
            cal.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        
        return cal.getTimeInMillis() * 1000L;
    } catch (Exception e) {
        log.error("Error converting datetime to UTC timestamp: {}", dateTime, e);
        throw new PaymentMappingException("Invalid datetime for UTC conversion: " + dateTime);
    }
}
```

---

## Business Date Validation

```java
@Named("validateBusinessDate")
Long validateBusinessDate(XMLGregorianCalendar date) {
    Long timestamp = convertToTimestampMicros(date);
    if (timestamp == null) return null;
    
    try {
        // Convert back to check business rules
        Instant instant = Instant.ofEpochMilli(timestamp / 1000L);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
        
        // Validate not weekend for business dates
        if (localDateTime.getDayOfWeek() == DayOfWeek.SATURDAY || 
            localDateTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
            log.warn("Payment date falls on weekend: {}", localDateTime);
        }
        
        // Validate not too far in future (more than 1 year)
        if (localDateTime.isAfter(LocalDateTime.now().plusYears(1))) {
            log.warn("Payment date more than 1 year in future: {}", localDateTime);
        }
        
        return timestamp;
    } catch (Exception e) {
        log.error("Error validating business date: {}", timestamp, e);
        return timestamp; // Return original if validation fails
    }
}
```

---

## Date Consistency Validation

```java
// Add to @AfterMapping method
private void validateDateConsistency(CanonicalBizView target) {
    // Ensure completion date is not before creation date
    if (target.getPaymentCreationDate() != null && target.getPaymentCompletionDate() != null) {
        if (target.getPaymentCompletionDate() < target.getPaymentCreationDate()) {
            throw new PaymentMappingException(
                "Payment completion date cannot be before creation date");
        }
    }
    
    // Ensure processing timestamp is recent (within 5 minutes)
    if (target.getPodsLastUpdatedDatetime() != null) {
        long currentTime = System.currentTimeMillis() * 1000L;
        long timeDiff = Math.abs(currentTime - target.getPodsLastUpdatedDatetime());
        
        // Warn if processing timestamp is more than 5 minutes off
        if (timeDiff > 5 * 60 * 1000 * 1000L) { // 5 minutes in microseconds
            log.warn("Processing timestamp significantly different from current time: {} minutes", 
                    timeDiff / (60 * 1000 * 1000L));
        }
    }
}
```

---

## Source Date Validation

```java
// Add to @BeforeMapping method
private void validateSourceDates(CustomerCreditTransferInitiationV10 source) {
    XMLGregorianCalendar creDtTm = source.getGrpHdr().getCreDtTm();
    if (!creDtTm.isValid()) {
        throw new PaymentMappingException("Invalid creation date/time format");
    }
    
    // Validate creation date is not too old (more than 1 year)
    try {
        long creationTime = creDtTm.toGregorianCalendar().getTimeInMillis();
        long oneYearAgo = System.currentTimeMillis() - (365L * 24 * 60 * 60 * 1000);
        
        if (creationTime < oneYearAgo) {
            log.warn("Payment creation date is more than 1 year old: {}", creDtTm);
        }
    } catch (Exception e) {
        log.error("Error validating creation date: {}", creDtTm, e);
    }
    
    // Validate required execution date if present
    PaymentInstruction34 pmtInf = source.getPmtInf().get(0);
    if (pmtInf.getReqdExctnDt() != null) {
        DateAndDateTime2Choice execDt = pmtInf.getReqdExctnDt();
        if (execDt.getDtTm() != null && !execDt.getDtTm().isValid()) {
            throw new PaymentMappingException("Invalid execution date/time format");
        }
        if (execDt.getDt() != null && !execDt.getDt().isValid()) {
            throw new PaymentMappingException("Invalid execution date format");
        }
    }
}
```

---

## Target Date Validation

```java
// Add to @AfterMapping method
private void validateTargetDates(CanonicalBizView target) {
    Objects.requireNonNull(target.getPaymentCreationDate(), "Payment creation date must be mapped");
    
    // Validate timestamp ranges
    if (target.getPaymentCreationDate() <= 0) {
        throw new PaymentMappingException("Payment creation date must be positive timestamp");
    }
    
    // Validate microsecond precision (should be multiple of 1000)
    if (target.getPaymentCreationDate() % 1000 != 0) {
        log.debug("Payment creation date has sub-millisecond precision: {}", 
                target.getPaymentCreationDate());
    }
    
    // Validate processing timestamp is set
    if (target.getPodsLastUpdatedDatetime() == null) {
        throw new PaymentMappingException("Processing timestamp must be set");
    }
}
```

---

## Required Imports

```java
import java.time.*;
import java.util.TimeZone;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.GregorianCalendar;
```

---

## Implementation Notes

1. **Always handle null values gracefully**
2. **Convert to microseconds (milliseconds * 1000L)**
3. **Handle timezone conversion to UTC**
4. **Validate date formats and ranges**
5. **Log warnings for business rule violations**
6. **Use try-catch for datetime conversion errors**
7. **Clone XMLGregorianCalendar before modification**

Generate these datetime helpers as Phase 3 after enumeration mappings are complete.
