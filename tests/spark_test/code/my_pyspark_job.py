from pyspark import SparkContext
from pyspark.sql import SQLContext
import time
import sys
import argparse
from pyspark.sql import SparkSession
from pyspark.sql.types import StructType,StructField, StringType, IntegerType


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument('--app_name', type=str, required=False, default=None, help="spark app name argument")
    args = parser.parse_args()
    spark = SparkSession.builder.appName('tuancam').getOrCreate()
    data = [
        ("James","","Smith","36636","M",3000),
        ("Michael","Rose","","40288","M",4000),
        ("Robert","","Williams","42114","M",4000),
        ("Maria","Anne","Jones","39192","F",4000),
        ("Jen","Mary","Brown","","F",-1)
    ]

    schema = StructType([ \
        StructField("firstname",StringType(),True), \
        StructField("middlename",StringType(),True), \
        StructField("lastname",StringType(),True), \
        StructField("id", StringType(), True), \
        StructField("gender", StringType(), True), \
        StructField("salary", IntegerType(), True) \
    ])
 
    df = spark.createDataFrame(data=data,schema=schema)
    df.printSchema()
    df.show(truncate=False)