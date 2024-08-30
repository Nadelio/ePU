echo $1 "is the file to be processed"

java -jar ExcelFormulaProcessor.jar $1 && echo "Processed successfully" || echo "Failed to process"