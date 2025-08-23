# Pain001 to CanonicalBizView - Date/Time Transformation Mappings (Medium Confidence)

**Project**: Data Transformator - Pain001 Field Mapping Matrix  
**Category**: Date/Time Transformations - Medium Confidence  
**Date**: August 22, 2025  
**Complexity**: MEDIUM - Format conversion and timezone handling  

---

## ISO DateTime to Timestamp-Micros Mappings

| Source Path | Source Type | Source Format | Target Path | Target Type | Target Format | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|---------------|-------------|-------------|---------------|-----------------|------------|----------------|
| grpHdr.creDtTm | ISODateTime | YYYY-MM-DDTHH:mm:ss.sssZ | payment_creation_date | timestamp-micros | Long (microseconds since epoch) | qualifiedByName | MEDIUM | DateTime conversion |
| grpHdr.creDtTm | ISODateTime | YYYY-MM-DDTHH:mm:ss.sssZ | Pods_last_updated_datetime | timestamp-micros | Long (microseconds since epoch) | qualifiedByName | MEDIUM | Same source, audit field |

### Implementation Pattern
```java
@Mapping(target = "payment_creation_date", source = "grpHdr.creDtTm", qualifiedByName = "convertToTimestampMicros")
@Mapping(target = "Pods_last_updated_datetime", source = "grpHdr.creDtTm", qualifiedByName = "convertToTimestampMicros")

@Named("convertToTimestampMicros")
Long convertToTimestampMicros(XMLGregorianCalendar dateTime) {
    if (dateTime == null) return null;
    return dateTime.toGregorianCalendar().getTimeInMillis() * 1000L;
}
```

---

## DateAndDateTime2Choice to Timestamp-Micros Mappings

| Source Path | Source Type | Source Format | Target Path | Target Type | Target Format | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|---------------|-------------|-------------|---------------|-----------------|------------|----------------|
| pmtInf.reqdExctnDt | DateAndDateTime2Choice | Date OR DateTime | payment_completion_date | timestamp-micros | Long (microseconds since epoch) | qualifiedByName | MEDIUM | Choice resolution + conversion |
| pmtInf.poolAndjstmntDt | ISODate | YYYY-MM-DD | pooling_adjustment_date | timestamp-micros | Long (microseconds since epoch) | qualifiedByName | MEDIUM | Date-only conversion |

### Implementation Pattern
```java
@Mapping(target = "payment_completion_date", source = "pmtInf.reqdExctnDt", qualifiedByName = "convertDateTimeChoiceToTimestampMicros")
@Mapping(target = "pooling_adjustment_date", source = "pmtInf.poolAdjstmntDt", qualifiedByName = "convertDateToTimestampMicros")

@Named("convertDateTimeChoiceToTimestampMicros")
Long convertDateTimeChoiceToTimestampMicros(DateAndDateTime2Choice choice) {
    if (choice == null) return null;
    
    if (choice.getDtTm() != null) {
        // DateTime present - convert directly
        return convertToTimestampMicros(choice.getDtTm());
    } else if (choice.getDt() != null) {
        // Date only - convert to midnight UTC
        XMLGregorianCalendar cal = choice.getDt();
        cal.setTime(0, 0, 0, 0); // Set to midnight
        return convertToTimestampMicros(cal);
    }
    return null;
}

@Named("convertDateToTimestampMicros")
Long convertDateToTimestampMicros(XMLGregorianCalendar date) {
    if (date == null) return null;
    
    // Clone to avoid modifying original
    XMLGregorianCalendar cal = (XMLGregorianCalendar) date.clone();
    cal.setTime(0, 0, 0, 0); // Set to midnight UTC
    return convertToTimestampMicros(cal);
}
```

---

## Optional Date Field Mappings

| Source Path | Source Type | Source Format | Target Path | Target Type | Target Format | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|---------------|-------------|-------------|---------------|-----------------|------------|----------------|
| cdtTrfTxInf.pmtId.txDt | ISODate | YYYY-MM-DD | transaction_date | timestamp-micros | Long (microseconds since epoch) | qualifiedByName | MEDIUM | Optional date conversion |
| pmtInf.pmtTpInf.svcLvl.cd | ServiceLevel8Choice | Service level with dates | service_level_date | timestamp-micros | Long (microseconds since epoch) | qualifiedByName | MEDIUM | Conditional date extraction |

### Implementation Pattern
```java
@Mapping(target = "transaction_date", source = "cdtTrfTxInf.pmtId.txDt", qualifiedByName = "convertDateToTimestampMicros")
@Mapping(target = "service_level_date", source = "pmtInf.pmtTpInf.svcLvl", qualifiedByName = "extractServiceLevelDate")

@Named("extractServiceLevelDate")
Long extractServiceLevelDate(ServiceLevel8Choice svcLvl) {
    // Extract date from service level if present
    if (svcLvl != null && svcLvl.getPrtry() != null) {
        // Custom logic to extract date from proprietary service level
        return parseServiceLevelDate(svcLvl.getPrtry());
    }
    return null;
}
```

---

## Current Timestamp Generation

| Source Path | Source Type | Source Format | Target Path | Target Type | Target Format | Mapping Strategy | Confidence | Implementation |
|-------------|-------------|---------------|-------------|-------------|---------------|-----------------|------------|----------------|
| (processing_time) | derived | Current system time | processing_timestamp | timestamp-micros | Long (microseconds since epoch) | expression | HIGH | Current time generation |

### Implementation Pattern
```java
@Mapping(target = "processing_timestamp", expression = "java(java.time.Instant.now().toEpochMilli() * 1000L)")
```

---

## Timezone Handling Considerations

### UTC Conversion Strategy
```java
@Named("convertToTimestampMicrosUTC")
Long convertToTimestampMicrosUTC(XMLGregorianCalendar dateTime) {
    if (dateTime == null) return null;
    
    // Convert to UTC if timezone is specified
    GregorianCalendar cal = dateTime.toGregorianCalendar();
    
    // If no timezone specified, assume UTC
    if (dateTime.getTimezone() == DatatypeConstants.FIELD_UNDEFINED) {
        cal.setTimeZone(TimeZone.getTimeZone("UTC"));
    }
    
    return cal.getTimeInMillis() * 1000L;
}
```

### Business Hours Validation
```java
@Named("validateBusinessDate")
Long validateBusinessDate(XMLGregorianCalendar date) {
    Long timestamp = convertToTimestampMicros(date);
    if (timestamp == null) return null;
    
    // Convert back to check business rules
    Instant instant = Instant.ofEpochMilli(timestamp / 1000L);
    LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
    
    // Validate not weekend for business dates
    if (localDateTime.getDayOfWeek() == DayOfWeek.SATURDAY || 
        localDateTime.getDayOfWeek() == DayOfWeek.SUNDAY) {
        log.warn("Payment date falls on weekend: {}", localDateTime);
    }
    
    return timestamp;
}
```

---

## Date Range and Validation Logic

### Future Date Validation
```java
@Named("validateFutureDate")
Long validateFutureDate(XMLGregorianCalendar date, String fieldName) {
    Long timestamp = convertToTimestampMicros(date);
    if (timestamp == null) return null;
    
    long currentTime = System.currentTimeMillis() * 1000L;
    
    // Warn if date is more than 1 year in future
    if (timestamp > currentTime + (365L * 24 * 60 * 60 * 1000 * 1000)) {
        log.warn("Date more than 1 year in future for {}: {}", fieldName, 
                Instant.ofEpochMilli(timestamp / 1000L));
    }
    
    return timestamp;
}
```

### Date Consistency Validation
```java
@AfterMapping
protected void validateDateConsistency(@MappingTarget CanonicalBizView target) {
    // Ensure completion date is not before creation date
    if (target.getPaymentCreationDate() != null && target.getPaymentCompletionDate() != null) {
        if (target.getPaymentCompletionDate() < target.getPaymentCreationDate()) {
            throw new PaymentMappingException(
                "Payment completion date cannot be before creation date");
        }
    }
    
    // Ensure processing timestamp is recent
    if (target.getProcessingTimestamp() != null) {
        long currentTime = System.currentTimeMillis() * 1000L;
        long timeDiff = Math.abs(currentTime - target.getProcessingTimestamp());
        
        // Warn if processing timestamp is more than 1 minute off
        if (timeDiff > 60 * 1000 * 1000L) { // 60 seconds in microseconds
            log.warn("Processing timestamp significantly different from current time");
        }
    }
}
```

---

## Validation Rules for Date/Time Mappings

### Source Validation (in @BeforeMapping)
```java
Objects.requireNonNull(source.getGrpHdr().getCreDtTm(), "Creation date/time is mandatory");

// Validate required execution date
PaymentInstruction34 pmtInf = source.getPmtInf().get(0);
Objects.requireNonNull(pmtInf.getReqdExctnDt(), "Required execution date is mandatory");

// Validate date formats (XMLGregorianCalendar should be valid)
XMLGregorianCalendar creDtTm = source.getGrpHdr().getCreDtTm();
if (!creDtTm.isValid()) {
    throw new PaymentMappingException("Invalid creation date/time format");
}
```

### Target Validation (in @AfterMapping)
```java
Objects.requireNonNull(target.getPaymentCreationDate(), "Payment creation date must be mapped");

// Validate timestamp ranges
if (target.getPaymentCreationDate() <= 0) {
    throw new PaymentMappingException("Payment creation date must be positive timestamp");
}

// Validate microsecond precision
if (target.getPaymentCreationDate() % 1000 != 0) {
    log.debug("Payment creation date has sub-millisecond precision");
}
```

---

## Test Scenarios for Date/Time Mappings

### Happy Path Tests
```java
@Test
public void testDateTimeConversion() {
    // Set creation date/time
    XMLGregorianCalendar creDtTm = createXMLGregorianCalendar("2025-08-22T14:30:00.000Z");
    pain001.getGrpHdr().setCreDtTm(creDtTm);
    
    CanonicalBizView result = mapper.map(pain001);
    
    // Verify timestamp conversion
    assertNotNull(result.getPaymentCreationDate());
    
    // Convert back to verify
    Instant instant = Instant.ofEpochMilli(result.getPaymentCreationDate() / 1000L);
    assertEquals("2025-08-22T14:30:00Z", instant.toString());
}

@Test
public void testDateChoiceConversion() {
    // Test DateTime choice
    DateAndDateTime2Choice choice = new DateAndDateTime2Choice();
    choice.setDtTm(createXMLGregorianCalendar("2025-08-23T09:00:00.000Z"));
    pain001.getPmtInf().get(0).setReqdExctnDt(choice);
    
    CanonicalBizView result = mapper.map(pain001);
    assertNotNull(result.getPaymentCompletionDate());
}

@Test
public void testDateOnlyConversion() {
    // Test Date-only choice (should convert to midnight UTC)
    DateAndDateTime2Choice choice = new DateAndDateTime2Choice();
    choice.setDt(createXMLGregorianCalendar("2025-08-23"));
    pain001.getPmtInf().get(0).setReqdExctnDt(choice);
    
    CanonicalBizView result = mapper.map(pain001);
    
    // Verify it's midnight UTC
    Instant instant = Instant.ofEpochMilli(result.getPaymentCompletionDate() / 1000L);
    assertEquals("2025-08-23T00:00:00Z", instant.toString());
}
```

### Edge Case Tests
```java
@Test
public void testNullDateHandling() {
    pain001.getPmtInf().get(0).setReqdExctnDt(null);
    CanonicalBizView result = mapper.map(pain001);
    assertNull(result.getPaymentCompletionDate());
}

@Test
public void testTimezoneHandling() {
    // Test various timezone inputs
    XMLGregorianCalendar easternTime = createXMLGregorianCalendar("2025-08-22T10:30:00.000-04:00");
    pain001.getGrpHdr().setCreDtTm(easternTime);
    
    CanonicalBizView result = mapper.map(pain001);
    
    // Should be converted to UTC (14:30:00Z)
    Instant instant = Instant.ofEpochMilli(result.getPaymentCreationDate() / 1000L);
    assertEquals("2025-08-22T14:30:00Z", instant.toString());
}

@Test
public void testDateConsistencyValidation() {
    // Set completion date before creation date (should fail validation)
    pain001.getGrpHdr().setCreDtTm(createXMLGregorianCalendar("2025-08-22T14:30:00.000Z"));
    
    DateAndDateTime2Choice choice = new DateAndDateTime2Choice();
    choice.setDtTm(createXMLGregorianCalendar("2025-08-22T10:00:00.000Z")); // Earlier than creation
    pain001.getPmtInf().get(0).setReqdExctnDt(choice);
    
    assertThrows(PaymentMappingException.class, () -> mapper.map(pain001));
}
```

### Performance Tests
```java
@Test
public void testDateConversionPerformance() {
    // Test conversion of many dates
    List<XMLGregorianCalendar> dates = generateTestDates(1000);
    
    long startTime = System.currentTimeMillis();
    for (XMLGregorianCalendar date : dates) {
        Long timestamp = mapper.convertToTimestampMicros(date);
        assertNotNull(timestamp);
    }
    long endTime = System.currentTimeMillis();
    
    // Should complete within reasonable time
    assertTrue("Date conversion too slow", (endTime - startTime) < 1000);
}
```

---

*This matrix covers all date/time transformation mappings from pain001 to canonicalBizView requiring format conversion and timezone handling.*
